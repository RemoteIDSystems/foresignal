/**
 * The news-article record shape shared by the news sources (prompt 133): {@code gdelt-articles} and the outlet feeds
 * of the {@code news-feed-*} family (073 feed kit, item mapper {@code news-article}) both write kind
 * {@code news-article} with the payload {@link ai.foresignal.source.formats.news.NewsArticles} declares.
 *
 * <ul>
 *   <li>{@link ai.foresignal.source.formats.news.ArticleUrls}: URL canonicalization and the SHA-256 external id;
 *   <li>{@link ai.foresignal.source.formats.news.NewsNames}: country and language names to ISO codes;
 *   <li>{@link ai.foresignal.source.formats.news.NewsOutletLookup}: how an adapter resolves an outlet id without
 *       reading the store itself.
 * </ul>
 *
 * <p>Articles are metadata only: no body and no byline is ever stored.
 */
package ai.foresignal.source.formats.news;
