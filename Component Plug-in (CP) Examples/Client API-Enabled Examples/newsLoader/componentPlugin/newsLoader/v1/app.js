let NewsLoadFriendlyName = "LoadNewsClientApi";
let NewsStoreFriendlyName = "StoreNewsClientApi";
const newsStructure = ["id", "title", "source", "date", "region", "content"];

state = {
    userDetails: {},
    news: [],
    region: null,
    sortBy: null,
    newsFreshness: null,
    canAddNews: null,
    connectedSystem: null
};

Appian.Component.onNewValue(async (newValues) => {
    state.userDetails = newValues.userDetails;
    state.region = newValues.region;
    state.sortBy = newValues.sortBy;
    state.newsFreshness = newValues.newsFreshness;
    state.canAddNews = newValues.canAddNews;
    state.connectedSystem = newValues.connectedSystem;

    state.news = await fetchNews(newValues.connectedSystem);
    render();
    attachEventListeners();
})

// Render app
function render() {
    const app = document.getElementById('app');
    app.innerHTML = `
        <div class="container">
            <header class="header">
                <div class="user-info">
                    <h1>Welcome back, ${state.userDetails.name}!</h1>
                    <p class="user-details">${state.userDetails.email} • ${state.userDetails.country}</p>
                </div>
            </header>

            <div class="controls">
                <h3>
                    Fetching News for you
                </h3>

                ${state.canAddNews != null && state.canAddNews === true ? '<button id="addNewsBtn" class="btn-primary">+ Add News</button>' : ''}
            </div>

            ${
                state.news?.length == 0 ? 
                `<div class="no-news">
                    no news to show
                </div>` :
                `<div class="news-grid">
                    ${
                        getFilteredNews().map(news =>`
                            <article class="news-card">
                                <div class="news-header">
                                    <span class="news-region">${news.region}</span>
                                    <span class="news-date">${formatDate(news.date)}</span>
                                </div>
                                <h2 class="news-title">${news.title}</h2>
                                <p class="news-content">${news.content}</p>
                                <div class="news-footer">
                                    <span class="news-source">${news.source}</span>
                                </div>
                            </article>
                        `).join('')
                    }
                </div>`
            }
        </div>

        <div id="modal" class="modal" style="display: none;">
            <div class="modal-content">
                <div class="modal-header">
                    <h2>Add News Article</h2>
                    <button id="closeModal" class="close-btn">&times;</button>
                </div>
                <form id="newsForm">
                    <div class="form-group">
                        <label>Title</label>
                        <input type="text" id="newsTitle" required>
                    </div>
                    <div class="form-group">
                        <label>Content</label>
                        <textarea id="newsContent" rows="4" required></textarea>
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label>Source</label>
                            <input type="text" id="newsSource" required>
                        </div>
                        <div class="form-group">
                            <label>Region</label>
                            <select id="newsRegion" required>
                                <option value="Africa">Africa</option>
                                <option value="Antarctica">Antarctica</option>
                                <option value="Asia">Asia</option>
                                <option value="Australia">Australia</option>
                                <option value="Europe">Europe</option>
                                <option value="North America">North America</option>
                                <option value="South America">South America</option>
                            </select>
                        </div>
                    </div>
                    <div class="form-actions">
                        <button type="button" id="cancelBtn" class="btn-secondary">Cancel</button>
                        <button type="submit" class="btn-primary">Add Article</button>
                    </div>
                </form>
            </div>
        </div>
    `;
}

// Filter and sort news
function getFilteredNews() {
    let filtered = [];
    for (let news of state.news) {
        try {
            let parsedNews = JSON.parse(news);

            if (
                parsedNews &&
                typeof parsedNews === "object" &&
                !Array.isArray(parsedNews) &&
                isValidNews(parsedNews)
            ) {
                filtered.push(parsedNews);
            }
        } catch (e) {
            console.error("Invalid JSON:", e);
        }
    }

    // Filter by freshness
    const now = new Date('2026-01-28');
    filtered = filtered.filter(news => {
        const newsDate = new Date(news.date);
        const daysDiff = Math.floor((now - newsDate) / (1000 * 60 * 60 * 24));

        switch(state.newsFreshness) {
            case 'latest': return daysDiff <= 3;
            case 'oneWeek': return daysDiff <= 7;
            case 'twoWeeks': return daysDiff <= 14;
            case 'oneMonth': return daysDiff <= 30;
            default: return true;
        }
    });

    // Sort
    filtered.sort((a, b) => {
        const dateA = new Date(a.date);
        const dateB = new Date(b.date);
        return state.sortBy === 'latest-first' ? dateB - dateA : dateA - dateB;
    });

    return filtered;
}

// Format date
function formatDate(dateStr) {
    const date = new Date(dateStr);
    const now = new Date();
    const daysDiff = Math.floor((now - date) / (1000 * 60 * 60 * 24));

    if (daysDiff === 0) return 'Today';
    if (daysDiff === 1) return 'Yesterday';
    if (daysDiff < 7) return `${daysDiff} days ago`;
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

// Event listeners
function attachEventListeners() {
    const addBtn = document.getElementById('addNewsBtn');
    const modal = document.getElementById('modal');
    const closeModal = document.getElementById('closeModal');
    const cancelBtn = document.getElementById('cancelBtn');
    const newsForm = document.getElementById('newsForm');
    const newsFetch = document.getElementById('fetchNews');

    if (addBtn) {
        addBtn.addEventListener('click', () => {
            modal.style.display = 'flex';
        });
    }

    if (closeModal) {
        closeModal.addEventListener('click', () => {
            modal.style.display = 'none';
            newsForm.reset();
        });
    }

    if (cancelBtn) {
        cancelBtn.addEventListener('click', () => {
            modal.style.display = 'none';
            newsForm.reset();
        });
    }

    if (newsForm) {
        newsForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const newArticle = {
                id: state.news?.length + 1,
                title: document.getElementById('newsTitle').value,
                content: document.getElementById('newsContent').value,
                source: document.getElementById('newsSource').value,
                region: document.getElementById('newsRegion').value,
                date: new Date().toISOString().split('T')[0]
            };

            await storeNews(newArticle);
            window.location.reload();
        });
    }

    if(newsFetch){
        newsFetch.addEventListener('click', () => {
            fetchNews(state.connectedSystem);
        })
    }

    modal?.addEventListener('click', (e) => {
        if (e.target === modal) {
            modal.style.display = 'none';
            newsForm.reset();
        }
    });
}

function isValidNews(news) {
  return newsStructure.every(key =>
    Object.prototype.hasOwnProperty.call(news, key)
  );
}

async function storeNews(params) {
    var connectedSystem = state.connectedSystem;
    var payload = {
        "news": JSON.stringify(params)
    };
    if(connectedSystem){
        const resp = await Appian.Component.invokeClientApi(connectedSystem, NewsStoreFriendlyName, payload, ["canAddNews", "saveNewsToFolder"]);
        return resp;
    }
}

async function fetchNews() {
    var connectedSystem = state.connectedSystem;
    if(connectedSystem){
        const resp = await Appian.Component.invokeClientApi(connectedSystem, NewsLoadFriendlyName, {}, ["saveNewsToFolder"]);

        if(resp.type === "INVOCATION_SUCCESS" && Object.prototype.hasOwnProperty.call(resp.payload, "news")){
            return resp.payload.news;
        } else {
            return [];
        }

    } else {
        console.error("Connected system not provided")
        return [];
    }
}
