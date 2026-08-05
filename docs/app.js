const SUPABASE_URL = "https://gbimvviaawgvsxqzyoxr.supabase.co/rest/v1/players?select=*";
const API_KEY = "sb_publishable_XqNh6YwQsMEBQkiI8-FUuw_3_xAZwo9";

const SKILLS = [
    "mining_xp", "woodcutting_xp", "fishing_xp", "smithing_xp", 
    "fletching_xp", "cooking_xp", "attack_xp", "defense_xp", 
    "strength_xp", "ranged_xp", "life_xp", "arcana_xp", 
    "magic_xp", "mobility_xp"
];

let allPlayers = [];
let currentSort = "total";
let currentSortAsc = false;
let showingCheaters = false;
let currentVersion = "all";

// Convert XP to Level using OSRS Formula
function getLevelAtExperience(xp) {
    if (xp <= 0) return 1;
    for (let level = 1; level <= 99; level++) {
        if (xp < getXpForLevel(level + 1)) {
            return level;
        }
    }
    return 99;
}

function getXpForLevel(level) {
    if (level <= 1) return 0;
    let total = 0;
    for (let i = 1; i < level; i++) {
        total += Math.floor(i + 300.0 * Math.pow(2.0, i / 7.0));
    }
    return Math.floor(total / 4.0);
}

// Format numbers with commas
function formatXP(xp) {
    if (xp >= 1000000) {
        return (xp / 1000000).toFixed(1) + 'M';
    } else if (xp >= 10000) {
        return (xp / 1000).toFixed(0) + 'k';
    }
    return xp.toLocaleString();
}

async function fetchLeaderboard() {
    try {
        const response = await fetch(SUPABASE_URL, {
            headers: {
                "apikey": API_KEY,
                "Authorization": `Bearer ${API_KEY}`
            }
        });

        if (!response.ok) throw new Error("Failed to fetch data");

        const data = await response.json();
        processData(data);
    } catch (error) {
        console.error("Error fetching leaderboard:", error);
        document.getElementById("loading-state").innerHTML = "<p>Error communing with the Cloud. Check console.</p>";
    }
}

function processData(data) {
    allPlayers = data.map(player => {
        let totalLevel = 0;
        let totalXp = 0;
        
        SKILLS.forEach(skill => {
            const xp = player[skill] || 0;
            totalLevel += getLevelAtExperience(xp);
            totalXp += xp;
        });

        return {
            ...player,
            mcVersionDisplay: player.mc_version || "Legacy",
            total: totalLevel,
            totalXp: totalXp
        };
    });

    populateVersionDropdown();
    document.getElementById("loading-state").classList.add("hidden");
    renderLeaderboard();
}

function populateVersionDropdown() {
    const versions = new Set(allPlayers.map(p => p.mcVersionDisplay));
    const dropdown = document.getElementById("version-dropdown");
    
    // Clear existing options except "all"
    dropdown.innerHTML = '<option value="all">Version: All</option>';
    
    // Sort versions (roughly)
    Array.from(versions).sort().reverse().forEach(version => {
        const option = document.createElement("option");
        option.value = version;
        option.innerText = `Version: ${version}`;
        dropdown.appendChild(option);
    });
}

let searchQuery = "";
let currentView = "all";

function renderLeaderboard() {
    const tbody = document.getElementById("leaderboard-body");
    const emptyState = document.getElementById("empty-state");
    tbody.innerHTML = "";

    // Sync header visibility
    document.querySelectorAll("th.skill-col").forEach(th => {
        if (currentView === "all" || th.dataset.sort === currentView) {
            th.classList.remove("hidden");
        } else {
            th.classList.add("hidden");
        }
    });

    // Filter
    let filteredPlayers = allPlayers.filter(p => p.cheater === showingCheaters);
    
    if (currentVersion !== "all") {
        filteredPlayers = filteredPlayers.filter(p => p.mcVersionDisplay === currentVersion);
    }
    
    if (searchQuery) {
        const query = searchQuery.toLowerCase();
        filteredPlayers = filteredPlayers.filter(p => p.username && p.username.toLowerCase().includes(query));
    }

    if (filteredPlayers.length === 0) {
        emptyState.classList.remove("hidden");
        return;
    } else {
        emptyState.classList.add("hidden");
    }

    // Sort
    filteredPlayers.sort((a, b) => {
        let valA = a[currentSort] || 0;
        let valB = b[currentSort] || 0;
        
        // Secondary sort by Total XP if Total Levels are tied
        if (currentSort === 'total' && valA === valB) {
            valA = a.totalXp;
            valB = b.totalXp;
        }
        
        if (valA < valB) return currentSortAsc ? -1 : 1;
        if (valA > valB) return currentSortAsc ? 1 : -1;
        return 0;
    });

    // Update Overview
    const playerText = filteredPlayers.length === 1 ? 'Player' : 'Players';
    document.getElementById("stats-overview").innerText = 
        `Showing ${filteredPlayers.length} ${showingCheaters ? 'Cheater' : 'Legit'} ${playerText}`;

    // Render
    filteredPlayers.forEach((player, index) => {
        const tr = document.createElement("tr");
        
        // Rank formatting
        let rankClass = "";
        if (index === 0) rankClass = "rank-1";
        else if (index === 1) rankClass = "rank-2";
        else if (index === 2) rankClass = "rank-3";

        let html = `
            <td class="rank-col ${rankClass}">#${index + 1}</td>
            <td class="player-col">${player.username}</td>
            <td class="total-col">${player.total.toLocaleString()}</td>
        `;

        SKILLS.forEach(skill => {
            if (currentView === "all" || currentView === skill) {
                const xp = player[skill] || 0;
                const lvl = getLevelAtExperience(xp);
                html += `<td class="skill-col">
                            ${lvl}
                            <span class="xp-subtext">${formatXP(xp)} XP</span>
                         </td>`;
            }
        });

        tr.innerHTML = html;
        tbody.appendChild(tr);
    });
}

function updateSortUI() {
    // Sync table headers
    document.querySelectorAll(".sortable").forEach(el => {
        el.classList.remove("active", "asc");
        // Remove existing sort icons
        const existingIcon = el.querySelector(".sort-icon");
        if (existingIcon) existingIcon.remove();
        
        if (el.dataset.sort === currentSort) {
            el.classList.add("active");
            if (currentSortAsc) el.classList.add("asc");
            el.innerHTML += ` <span class="sort-icon">▼</span>`;
        }
    });
}

// Event Listeners
document.getElementById("search-input").addEventListener("input", (e) => {
    searchQuery = e.target.value.trim();
    renderLeaderboard();
});

document.getElementById("view-dropdown").addEventListener("change", (e) => {
    currentView = e.target.value;
    
    // Automatically sort by the selected skill (if not "all")
    if (currentView !== "all") {
        currentSort = currentView;
        currentSortAsc = false;
    } else {
        currentSort = "total";
        currentSortAsc = false;
    }
    
    updateSortUI();
    renderLeaderboard();
});

document.getElementById("theme-dropdown").addEventListener("change", (e) => {
    const theme = e.target.value;
    document.body.className = theme;
    localStorage.setItem("osl-theme", theme);
});

document.getElementById("version-dropdown").addEventListener("change", (e) => {
    currentVersion = e.target.value;
    renderLeaderboard();
});

document.querySelectorAll(".sortable").forEach(th => {
    th.addEventListener("click", () => {
        const sortKey = th.dataset.sort;
        
        if (currentSort === sortKey) {
            currentSortAsc = !currentSortAsc;
        } else {
            currentSort = sortKey;
            currentSortAsc = false;
        }

        updateSortUI();
        renderLeaderboard();
    });
});

document.getElementById("btn-legit").addEventListener("click", (e) => {
    showingCheaters = false;
    document.getElementById("btn-legit").classList.add("active");
    document.getElementById("btn-cheaters").classList.remove("active");
    renderLeaderboard();
});

document.getElementById("btn-cheaters").addEventListener("click", (e) => {
    showingCheaters = true;
    document.getElementById("btn-cheaters").classList.add("active");
    document.getElementById("btn-legit").classList.remove("active");
    renderLeaderboard();
});

// Init
fetchLeaderboard();
