console.log(
    "Bharat Smart Search - DSA Frontend Loaded"
);


/* =====================================================
   API
===================================================== */

const API_URL =
    "http://localhost:8080";


/* =====================================================
   ELEMENTS
===================================================== */

const searchInput =
    document.getElementById(
        "searchInput"
    );

const searchButton =
    document.getElementById(
        "searchButton"
    );

const clearButton =
    document.getElementById(
        "clearButton"
    );

const languageSelect =
    document.getElementById(
        "languageSelect"
    );

const suggestionsBox =
    document.getElementById(
        "suggestions"
    );

const performanceSection =
    document.getElementById(
        "performance"
    );

const algorithmsSection =
    document.getElementById(
        "algorithms"
    );

const resultsSection =
    document.getElementById(
        "resultsSection"
    );

const resultsContainer =
    document.getElementById(
        "resultsContainer"
    );


/* =====================================================
   SEARCH STATE
===================================================== */

let suggestionTimer = null;


/* =====================================================
   SEARCH
===================================================== */

async function performSearch(query) {

    query = query.trim();


    if (!query) {

        return;
    }


    suggestionsBox.innerHTML = "";

    suggestionsBox.style.display =
        "none";


    resultsSection.classList.remove(
        "hidden"
    );

    performanceSection.classList.remove(
        "hidden"
    );

    algorithmsSection.classList.remove(
        "hidden"
    );


    resultsContainer.innerHTML =
        `<div class="loading">
            🔎 Searching Wikipedia and running DSA algorithms...
        </div>`;


    const language =
        languageSelect.value;


    const start =
        performance.now();


    try {

        const url =
            API_URL
            + "/api/search?q="
            + encodeURIComponent(query)
            + "&lang="
            + encodeURIComponent(language);


        const response =
            await fetch(url);


        if (!response.ok) {

            throw new Error(
                "Server returned "
                + response.status
            );
        }


        const data =
            await response.json();


        const clientTime =
            performance.now()
            - start;


        displayMetrics(
            data,
            clientTime,
            query
        );


        displayResults(
            data.results || []
        );


        updateSearchHistory(
            query
        );


    } catch (error) {

        console.error(
            error
        );


        resultsContainer.innerHTML =
            `<div class="error-message">
                ❌ Could not connect to the Java server.
                <br><br>
                Make sure ApiServer is running on port 8080.
            </div>`;
    }
}


/* =====================================================
   DISPLAY METRICS
===================================================== */

function displayMetrics(
    data,
    clientTime,
    query
) {

    document.getElementById(
        "searchedText"
    ).textContent =
        `"${query}"`;


    document.getElementById(
        "searchTime"
    ).textContent =
        formatTime(
            data.searchTime
        );


    document.getElementById(
        "dsaTime"
    ).textContent =
        formatTime(
            data.dsaSearchTime
        );


    document.getElementById(
        "documentCount"
    ).textContent =
        data.documentCount ?? 0;


    document.getElementById(
        "matchedDocuments"
    ).textContent =
        data.matchedDocuments ?? 0;


    document.getElementById(
        "keywordCount"
    ).textContent =
        data.keywordCount ?? 0;


    document.getElementById(
        "resultCount"
    ).textContent =
        data.resultCount ?? 0;


    document.getElementById(
        "resultCountText"
    ).textContent =
        `${data.resultCount ?? 0} results`;


    document.getElementById(
        "lastSearch"
    ).textContent =
        `Completed in ${formatTime(data.searchTime)}`;
}


/* =====================================================
   TIME FORMAT
===================================================== */

function formatTime(
    milliseconds
) {

    const value =
        Number(milliseconds);


    if (
        Number.isNaN(value)
    ) {

        return "0 ms";
    }


    if (value < 1) {

        return (
            value.toFixed(3)
            + " ms"
        );
    }


    return (
        value.toFixed(2)
        + " ms"
    );
}


/* =====================================================
   DISPLAY RESULTS
===================================================== */

function displayResults(
    results
) {

    if (!results.length) {

        resultsContainer.innerHTML =
            `<div class="error-message">
                No matching documents were found.
            </div>`;

        return;
    }


    resultsContainer.innerHTML =
        results
            .map(
                (
                    result,
                    index
                ) => {

                    const title =
                        escapeHTML(
                            result.title
                            || "Untitled"
                        );


                    const text =
                        escapeHTML(
                            result.text
                            || "No snippet available."
                        );


                    const frequency =
                        result.frequency
                        ?? 0;


                    const url =
                        escapeAttribute(
                            result.url
                            || "#"
                        );


                    return `
                    <article class="result-card">

                        <div class="result-top">

                            <div class="result-number">

                                ${String(
                                    index + 1
                                ).padStart(
                                    2,
                                    "0"
                                )}

                            </div>


                            <div>

                                <h3 class="result-title">

                                    ${title}

                                </h3>


                                <div class="result-meta">

                                    <span class="meta-badge highlight">

                                        🎯 Frequency:
                                        ${frequency}

                                    </span>


                                    <span class="meta-badge">

                                        🔎 Inverted Index

                                    </span>


                                    <span class="meta-badge">

                                        ↕ Merge Sort

                                    </span>

                                </div>

                            </div>

                        </div>


                        <p class="result-snippet">

                            ${text}

                        </p>


                        <div class="result-meta">

                            <span class="meta-badge">

                                🧠 HashMap

                            </span>


                            <span class="meta-badge">

                                📊 Frequency Ranking

                            </span>


                            <a
                                class="open-link"
                                href="${url}"
                                target="_blank"
                                rel="noopener noreferrer"
                            >

                                OPEN WIKIPEDIA →

                            </a>

                        </div>

                    </article>
                    `;
                }
            )
            .join("");
}


/* =====================================================
   LIVE SUGGESTIONS
===================================================== */

searchInput.addEventListener(
    "input",
    () => {

        const prefix =
            searchInput.value.trim();


        clearTimeout(
            suggestionTimer
        );


        if (
            prefix.length < 2
        ) {

            suggestionsBox.innerHTML =
                "";

            suggestionsBox.style.display =
                "none";

            return;
        }


        suggestionTimer =
            setTimeout(
                () => {

                    loadSuggestions(
                        prefix
                    );

                },
                350
            );
    }
);


/* =====================================================
   LOAD SUGGESTIONS
===================================================== */

async function loadSuggestions(
    prefix
) {

    try {

        const language =
            languageSelect.value;


        const url =
            API_URL
            + "/api/suggestions?prefix="
            + encodeURIComponent(prefix)
            + "&lang="
            + encodeURIComponent(language);


        const response =
            await fetch(url);


        if (!response.ok) {

            return;
        }


        const data =
            await response.json();


        const suggestions =
            data.suggestions || [];


        if (
            suggestions.length === 0
        ) {

            suggestionsBox.style.display =
                "none";

            return;
        }


        suggestionsBox.innerHTML =
            suggestions
                .map(
                    suggestion => {

                        return `
                        <div
                            class="suggestion-item"
                            data-value="${escapeAttribute(suggestion)}"
                        >
                            🔍
                            ${escapeHTML(suggestion)}
                        </div>
                        `;
                    }
                )
                .join("");


        suggestionsBox.style.display =
            "block";


        document
            .querySelectorAll(
                ".suggestion-item"
            )
            .forEach(
                item => {

                    item.addEventListener(
                        "click",
                        () => {

                            const value =
                                item.dataset.value;


                            searchInput.value =
                                value;


                            suggestionsBox.style.display =
                                "none";


                            performSearch(
                                value
                            );
                        }
                    );
                }
            );


    } catch (error) {

        console.error(
            "Suggestion error:",
            error
        );
    }
}


/* =====================================================
   SEARCH BUTTON
===================================================== */

searchButton.addEventListener(
    "click",
    () => {

        performSearch(
            searchInput.value
        );
    }
);


/* =====================================================
   ENTER KEY
===================================================== */

searchInput.addEventListener(
    "keydown",
    event => {

        if (
            event.key === "Enter"
        ) {

            event.preventDefault();


            performSearch(
                searchInput.value
            );
        }
    }
);


/* =====================================================
   CLEAR
===================================================== */

clearButton.addEventListener(
    "click",
    () => {

        searchInput.value = "";

        suggestionsBox.innerHTML = "";

        suggestionsBox.style.display =
            "none";

        searchInput.focus();
    }
);


/* =====================================================
   LANGUAGE
===================================================== */

languageSelect.addEventListener(
    "change",
    () => {

        suggestionsBox.innerHTML = "";

        suggestionsBox.style.display =
            "none";
    }
);


/* =====================================================
   QUICK SEARCH
===================================================== */

document
    .querySelectorAll(
        ".quick-button"
    )
    .forEach(
        button => {

            button.addEventListener(
                "click",
                () => {

                    const query =
                        button.dataset.query;


                    searchInput.value =
                        query;


                    performSearch(
                        query
                    );
                }
            );
        }
    );


/* =====================================================
   THEME
===================================================== */

const themeButton =
    document.getElementById(
        "themeButton"
    );


const savedTheme =
    localStorage.getItem(
        "bharat-search-theme"
    );


if (
    savedTheme === "dark"
) {

    document.body.classList.add(
        "dark"
    );
}


themeButton.addEventListener(
    "click",
    () => {

        document.body.classList.toggle(
            "dark"
        );


        const isDark =
            document.body.classList.contains(
                "dark"
            );


        localStorage.setItem(
            "bharat-search-theme",
            isDark
                ? "dark"
                : "light"
        );
    }
);


/* =====================================================
   SEARCH HISTORY
===================================================== */

function updateSearchHistory(
    query
) {

    let count =
        Number(
            localStorage.getItem(
                "bharat-search-count"
            )
        )
        || 0;


    count++;


    localStorage.setItem(
        "bharat-search-count",
        count
    );
}


/* =====================================================
   HTML ESCAPING
===================================================== */

function escapeHTML(
    value
) {

    if (
        value === null
        ||
        value === undefined
    ) {

        return "";
    }


    return String(value)
        .replace(
            /&/g,
            "&amp;"
        )
        .replace(
            /</g,
            "&lt;"
        )
        .replace(
            />/g,
            "&gt;"
        )
        .replace(
            /"/g,
            "&quot;"
        )
        .replace(
            /'/g,
            "&#039;"
        );
}


function escapeAttribute(
    value
) {

    return escapeHTML(
        value
    );
}


/* =====================================================
   HIDE SUGGESTIONS WHEN CLICKING OUTSIDE
===================================================== */

document.addEventListener(
    "click",
    event => {

        if (
            !event.target.closest(
                ".search-area"
            )
        ) {

            suggestionsBox.style.display =
                "none";
        }
    }
);