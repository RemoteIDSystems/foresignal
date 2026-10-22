/**
 * Amtraker (prompt 080): {@code amtraker}, the positions of Amtrak, VIA Rail and Brightline trains from the
 * community Amtraker API ({@code api.amtraker.com/v3/trains}), an unofficial relay of the operators' own map data.
 *
 * <p>Terms, verified 2026-10-13 in the API's README ({@code github.com/piemadd/amtrak}, commit
 * {@code 126de6a6854df0b86bdfb9843d47e41369088a77}): the data is licensed under ODC-By 1.0 (adopted 2026-08-19),
 * attribution to Amtraker with a link to {@code amtraker.com} is required, and requests must carry a User-Agent that
 * identifies the project (Foresignal's always does). No rate limit is published, so the source polls gently (60 s)
 * and, needing no key, starts enabled (prompt 145); it is dropped if Amtraker or the operators object. The library
 * code in that repository is AGPL and was not used; only the documented response fields were read.
 */
package ai.foresignal.source.adapters.amtraker;
