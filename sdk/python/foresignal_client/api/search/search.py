import datetime
from http import HTTPStatus
from typing import Any
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.count_result import CountResult
from ...models.problem import Problem
from ...models.search_doc_type_item import SearchDocTypeItem
from ...models.search_licence_policy_item import SearchLicencePolicyItem
from ...models.search_mode import SearchMode
from ...models.search_results import SearchResults
from ...models.search_time_field import SearchTimeField
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    q: str,
    mode: SearchMode | Unset = SearchMode.HYBRID,
    doc_type: list[SearchDocTypeItem] | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    until: datetime.datetime | Unset = UNSET,
    limit: int | Unset = 25,
    time_field: SearchTimeField | Unset = SearchTimeField.EFFECTIVE,
    count: bool | Unset = False,
    place: list[str] | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    view: str | Unset = UNSET,
    in_watchlist: UUID | Unset = UNSET,
    country: list[str] | Unset = UNSET,
    admin1: list[str] | Unset = UNSET,
    county: list[str] | Unset = UNSET,
    licence_policy: list[SearchLicencePolicyItem] | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(foresignal_licence_policy, Unset):
        headers["Foresignal-Licence-Policy"] = foresignal_licence_policy

    params: dict[str, Any] = {}

    params["q"] = q

    json_mode: str | Unset = UNSET
    if not isinstance(mode, Unset):
        json_mode = mode.value

    params["mode"] = json_mode

    json_doc_type: list[str] | Unset = UNSET
    if not isinstance(doc_type, Unset):
        json_doc_type = []
        for doc_type_item_data in doc_type:
            doc_type_item = doc_type_item_data.value
            json_doc_type.append(doc_type_item)

    params["docType"] = json_doc_type

    json_source: list[str] | Unset = UNSET
    if not isinstance(source, Unset):
        json_source = source

    params["source"] = json_source

    json_kind: list[str] | Unset = UNSET
    if not isinstance(kind, Unset):
        json_kind = kind

    params["kind"] = json_kind

    params["bbox"] = bbox

    json_since: str | Unset = UNSET
    if not isinstance(since, Unset):
        json_since = since.isoformat()
    params["since"] = json_since

    json_until: str | Unset = UNSET
    if not isinstance(until, Unset):
        json_until = until.isoformat()
    params["until"] = json_until

    params["limit"] = limit

    json_time_field: str | Unset = UNSET
    if not isinstance(time_field, Unset):
        json_time_field = time_field.value

    params["timeField"] = json_time_field

    params["count"] = count

    json_place: list[str] | Unset = UNSET
    if not isinstance(place, Unset):
        json_place = place

    params["place"] = json_place

    params["radiusKm"] = radius_km

    params["view"] = view

    json_in_watchlist: str | Unset = UNSET
    if not isinstance(in_watchlist, Unset):
        json_in_watchlist = str(in_watchlist)
    params["inWatchlist"] = json_in_watchlist

    json_country: list[str] | Unset = UNSET
    if not isinstance(country, Unset):
        json_country = country

    params["country"] = json_country

    json_admin1: list[str] | Unset = UNSET
    if not isinstance(admin1, Unset):
        json_admin1 = admin1

    params["admin1"] = json_admin1

    json_county: list[str] | Unset = UNSET
    if not isinstance(county, Unset):
        json_county = county

    params["county"] = json_county

    json_licence_policy: list[str] | Unset = UNSET
    if not isinstance(licence_policy, Unset):
        json_licence_policy = []
        for licence_policy_item_data in licence_policy:
            licence_policy_item = licence_policy_item_data.value
            json_licence_policy.append(licence_policy_item)

    params["licencePolicy"] = json_licence_policy

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/search",
        "params": params,
    }

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> CountResult | SearchResults | Problem | None:
    if response.status_code == 200:

        def _parse_response_200(data: object) -> CountResult | SearchResults:
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                response_200_type_0 = SearchResults.from_dict(data)

                return response_200_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            if not isinstance(data, dict):
                raise TypeError()
            response_200_type_1 = CountResult.from_dict(data)

            return response_200_type_1

        response_200 = _parse_response_200(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[CountResult | SearchResults | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    q: str,
    mode: SearchMode | Unset = SearchMode.HYBRID,
    doc_type: list[SearchDocTypeItem] | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    until: datetime.datetime | Unset = UNSET,
    limit: int | Unset = 25,
    time_field: SearchTimeField | Unset = SearchTimeField.EFFECTIVE,
    count: bool | Unset = False,
    place: list[str] | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    view: str | Unset = UNSET,
    in_watchlist: UUID | Unset = UNSET,
    country: list[str] | Unset = UNSET,
    admin1: list[str] | Unset = UNSET,
    county: list[str] | Unset = UNSET,
    licence_policy: list[SearchLicencePolicyItem] | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Response[CountResult | SearchResults | Problem]:
    """Search records, briefings and alerts

     `text` ranks `websearch_to_tsquery` matches (plus `record_head.search_text` matches for records
    without documents) and always works; `semantic` embeds `q` with the active model (budgeted) and
    orders by cosine distance; `hybrid` (default) fuses both with reciprocal rank fusion (k = 60) and
    degrades to text with `degraded=true` when semantic search is unavailable (`semantic` then → 409
    SEMANTIC_UNAVAILABLE). Time filters apply to the effective time by default (`timeField=observed` for
    the observation time). When results degrade to text, `reason` says why. An unknown source is 404, a
    private one the caller may not see is 404 too. Record hits carry the persisted `seq`. Private
    sources are excluded unless the caller may see them; results are clamped to the caller's restriction
    (035).

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        q (str):
        mode (SearchMode | Unset):  Default: SearchMode.HYBRID.
        doc_type (list[SearchDocTypeItem] | Unset):
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        since (datetime.datetime | Unset):
        until (datetime.datetime | Unset):
        limit (int | Unset):  Default: 25.
        time_field (SearchTimeField | Unset):  Default: SearchTimeField.EFFECTIVE.
        count (bool | Unset):  Default: False.
        place (list[str] | Unset):
        radius_km (float | Unset):
        view (str | Unset):
        in_watchlist (UUID | Unset):
        country (list[str] | Unset):
        admin1 (list[str] | Unset):
        county (list[str] | Unset):
        licence_policy (list[SearchLicencePolicyItem] | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[CountResult | SearchResults | Problem]
    """

    kwargs = _get_kwargs(
        q=q,
        mode=mode,
        doc_type=doc_type,
        source=source,
        kind=kind,
        bbox=bbox,
        since=since,
        until=until,
        limit=limit,
        time_field=time_field,
        count=count,
        place=place,
        radius_km=radius_km,
        view=view,
        in_watchlist=in_watchlist,
        country=country,
        admin1=admin1,
        county=county,
        licence_policy=licence_policy,
        foresignal_licence_policy=foresignal_licence_policy,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    q: str,
    mode: SearchMode | Unset = SearchMode.HYBRID,
    doc_type: list[SearchDocTypeItem] | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    until: datetime.datetime | Unset = UNSET,
    limit: int | Unset = 25,
    time_field: SearchTimeField | Unset = SearchTimeField.EFFECTIVE,
    count: bool | Unset = False,
    place: list[str] | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    view: str | Unset = UNSET,
    in_watchlist: UUID | Unset = UNSET,
    country: list[str] | Unset = UNSET,
    admin1: list[str] | Unset = UNSET,
    county: list[str] | Unset = UNSET,
    licence_policy: list[SearchLicencePolicyItem] | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> CountResult | SearchResults | Problem | None:
    """Search records, briefings and alerts

     `text` ranks `websearch_to_tsquery` matches (plus `record_head.search_text` matches for records
    without documents) and always works; `semantic` embeds `q` with the active model (budgeted) and
    orders by cosine distance; `hybrid` (default) fuses both with reciprocal rank fusion (k = 60) and
    degrades to text with `degraded=true` when semantic search is unavailable (`semantic` then → 409
    SEMANTIC_UNAVAILABLE). Time filters apply to the effective time by default (`timeField=observed` for
    the observation time). When results degrade to text, `reason` says why. An unknown source is 404, a
    private one the caller may not see is 404 too. Record hits carry the persisted `seq`. Private
    sources are excluded unless the caller may see them; results are clamped to the caller's restriction
    (035).

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        q (str):
        mode (SearchMode | Unset):  Default: SearchMode.HYBRID.
        doc_type (list[SearchDocTypeItem] | Unset):
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        since (datetime.datetime | Unset):
        until (datetime.datetime | Unset):
        limit (int | Unset):  Default: 25.
        time_field (SearchTimeField | Unset):  Default: SearchTimeField.EFFECTIVE.
        count (bool | Unset):  Default: False.
        place (list[str] | Unset):
        radius_km (float | Unset):
        view (str | Unset):
        in_watchlist (UUID | Unset):
        country (list[str] | Unset):
        admin1 (list[str] | Unset):
        county (list[str] | Unset):
        licence_policy (list[SearchLicencePolicyItem] | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        CountResult | SearchResults | Problem
    """

    return sync_detailed(
        client=client,
        q=q,
        mode=mode,
        doc_type=doc_type,
        source=source,
        kind=kind,
        bbox=bbox,
        since=since,
        until=until,
        limit=limit,
        time_field=time_field,
        count=count,
        place=place,
        radius_km=radius_km,
        view=view,
        in_watchlist=in_watchlist,
        country=country,
        admin1=admin1,
        county=county,
        licence_policy=licence_policy,
        foresignal_licence_policy=foresignal_licence_policy,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    q: str,
    mode: SearchMode | Unset = SearchMode.HYBRID,
    doc_type: list[SearchDocTypeItem] | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    until: datetime.datetime | Unset = UNSET,
    limit: int | Unset = 25,
    time_field: SearchTimeField | Unset = SearchTimeField.EFFECTIVE,
    count: bool | Unset = False,
    place: list[str] | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    view: str | Unset = UNSET,
    in_watchlist: UUID | Unset = UNSET,
    country: list[str] | Unset = UNSET,
    admin1: list[str] | Unset = UNSET,
    county: list[str] | Unset = UNSET,
    licence_policy: list[SearchLicencePolicyItem] | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Response[CountResult | SearchResults | Problem]:
    """Search records, briefings and alerts

     `text` ranks `websearch_to_tsquery` matches (plus `record_head.search_text` matches for records
    without documents) and always works; `semantic` embeds `q` with the active model (budgeted) and
    orders by cosine distance; `hybrid` (default) fuses both with reciprocal rank fusion (k = 60) and
    degrades to text with `degraded=true` when semantic search is unavailable (`semantic` then → 409
    SEMANTIC_UNAVAILABLE). Time filters apply to the effective time by default (`timeField=observed` for
    the observation time). When results degrade to text, `reason` says why. An unknown source is 404, a
    private one the caller may not see is 404 too. Record hits carry the persisted `seq`. Private
    sources are excluded unless the caller may see them; results are clamped to the caller's restriction
    (035).

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        q (str):
        mode (SearchMode | Unset):  Default: SearchMode.HYBRID.
        doc_type (list[SearchDocTypeItem] | Unset):
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        since (datetime.datetime | Unset):
        until (datetime.datetime | Unset):
        limit (int | Unset):  Default: 25.
        time_field (SearchTimeField | Unset):  Default: SearchTimeField.EFFECTIVE.
        count (bool | Unset):  Default: False.
        place (list[str] | Unset):
        radius_km (float | Unset):
        view (str | Unset):
        in_watchlist (UUID | Unset):
        country (list[str] | Unset):
        admin1 (list[str] | Unset):
        county (list[str] | Unset):
        licence_policy (list[SearchLicencePolicyItem] | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[CountResult | SearchResults | Problem]
    """

    kwargs = _get_kwargs(
        q=q,
        mode=mode,
        doc_type=doc_type,
        source=source,
        kind=kind,
        bbox=bbox,
        since=since,
        until=until,
        limit=limit,
        time_field=time_field,
        count=count,
        place=place,
        radius_km=radius_km,
        view=view,
        in_watchlist=in_watchlist,
        country=country,
        admin1=admin1,
        county=county,
        licence_policy=licence_policy,
        foresignal_licence_policy=foresignal_licence_policy,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    q: str,
    mode: SearchMode | Unset = SearchMode.HYBRID,
    doc_type: list[SearchDocTypeItem] | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    until: datetime.datetime | Unset = UNSET,
    limit: int | Unset = 25,
    time_field: SearchTimeField | Unset = SearchTimeField.EFFECTIVE,
    count: bool | Unset = False,
    place: list[str] | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    view: str | Unset = UNSET,
    in_watchlist: UUID | Unset = UNSET,
    country: list[str] | Unset = UNSET,
    admin1: list[str] | Unset = UNSET,
    county: list[str] | Unset = UNSET,
    licence_policy: list[SearchLicencePolicyItem] | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> CountResult | SearchResults | Problem | None:
    """Search records, briefings and alerts

     `text` ranks `websearch_to_tsquery` matches (plus `record_head.search_text` matches for records
    without documents) and always works; `semantic` embeds `q` with the active model (budgeted) and
    orders by cosine distance; `hybrid` (default) fuses both with reciprocal rank fusion (k = 60) and
    degrades to text with `degraded=true` when semantic search is unavailable (`semantic` then → 409
    SEMANTIC_UNAVAILABLE). Time filters apply to the effective time by default (`timeField=observed` for
    the observation time). When results degrade to text, `reason` says why. An unknown source is 404, a
    private one the caller may not see is 404 too. Record hits carry the persisted `seq`. Private
    sources are excluded unless the caller may see them; results are clamped to the caller's restriction
    (035).

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        q (str):
        mode (SearchMode | Unset):  Default: SearchMode.HYBRID.
        doc_type (list[SearchDocTypeItem] | Unset):
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        since (datetime.datetime | Unset):
        until (datetime.datetime | Unset):
        limit (int | Unset):  Default: 25.
        time_field (SearchTimeField | Unset):  Default: SearchTimeField.EFFECTIVE.
        count (bool | Unset):  Default: False.
        place (list[str] | Unset):
        radius_km (float | Unset):
        view (str | Unset):
        in_watchlist (UUID | Unset):
        country (list[str] | Unset):
        admin1 (list[str] | Unset):
        county (list[str] | Unset):
        licence_policy (list[SearchLicencePolicyItem] | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        CountResult | SearchResults | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            q=q,
            mode=mode,
            doc_type=doc_type,
            source=source,
            kind=kind,
            bbox=bbox,
            since=since,
            until=until,
            limit=limit,
            time_field=time_field,
            count=count,
            place=place,
            radius_km=radius_km,
            view=view,
            in_watchlist=in_watchlist,
            country=country,
            admin1=admin1,
            county=county,
            licence_policy=licence_policy,
            foresignal_licence_policy=foresignal_licence_policy,
        )
    ).parsed
