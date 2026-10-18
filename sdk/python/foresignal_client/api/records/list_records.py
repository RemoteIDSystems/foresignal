import datetime
from http import HTTPStatus
from typing import Any, cast
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.count_result import CountResult
from ...models.list_records_format import ListRecordsFormat
from ...models.list_records_include_item import ListRecordsIncludeItem
from ...models.list_records_licence_policy_item import ListRecordsLicencePolicyItem
from ...models.list_records_order import ListRecordsOrder
from ...models.list_records_sort import ListRecordsSort
from ...models.list_records_time_field import ListRecordsTimeField
from ...models.problem import Problem
from ...models.record_page import RecordPage
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    until: datetime.datetime | Unset = UNSET,
    q: str | Unset = UNSET,
    include_expired: bool | Unset = False,
    format_: ListRecordsFormat | Unset = ListRecordsFormat.JSON,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 500,
    external_id: list[str] | Unset = UNSET,
    near: str | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    time_field: ListRecordsTimeField | Unset = ListRecordsTimeField.EFFECTIVE,
    order: ListRecordsOrder | Unset = ListRecordsOrder.OBSERVED,
    sort: ListRecordsSort | Unset = UNSET,
    as_of_seq: int | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
    include: list[ListRecordsIncludeItem] | Unset = UNSET,
    fields: str | Unset = UNSET,
    precision: int | Unset = 7,
    simplify: float | Unset = UNSET,
    place: list[str] | Unset = UNSET,
    view: str | Unset = UNSET,
    in_watchlist: UUID | Unset = UNSET,
    count: bool | Unset = False,
    country: list[str] | Unset = UNSET,
    admin1: list[str] | Unset = UNSET,
    county: list[str] | Unset = UNSET,
    licence_policy: list[ListRecordsLicencePolicyItem] | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(if_none_match, Unset):
        headers["If-None-Match"] = if_none_match

    if not isinstance(foresignal_licence_policy, Unset):
        headers["Foresignal-Licence-Policy"] = foresignal_licence_policy

    params: dict[str, Any] = {}

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

    params["q"] = q

    params["includeExpired"] = include_expired

    json_format_: str | Unset = UNSET
    if not isinstance(format_, Unset):
        json_format_ = format_.value

    params["format"] = json_format_

    params["cursor"] = cursor

    params["limit"] = limit

    json_external_id: list[str] | Unset = UNSET
    if not isinstance(external_id, Unset):
        json_external_id = external_id

    params["externalId"] = json_external_id

    params["near"] = near

    params["radiusKm"] = radius_km

    json_time_field: str | Unset = UNSET
    if not isinstance(time_field, Unset):
        json_time_field = time_field.value

    params["timeField"] = json_time_field

    json_order: str | Unset = UNSET
    if not isinstance(order, Unset):
        json_order = order.value

    params["order"] = json_order

    json_sort: str | Unset = UNSET
    if not isinstance(sort, Unset):
        json_sort = sort.value

    params["sort"] = json_sort

    params["asOfSeq"] = as_of_seq

    json_as_of: str | Unset = UNSET
    if not isinstance(as_of, Unset):
        json_as_of = as_of.isoformat()
    params["asOf"] = json_as_of

    json_include: list[str] | Unset = UNSET
    if not isinstance(include, Unset):
        json_include = []
        for include_item_data in include:
            include_item = include_item_data.value
            json_include.append(include_item)

    params["include"] = json_include

    params["fields"] = fields

    params["precision"] = precision

    params["simplify"] = simplify

    json_place: list[str] | Unset = UNSET
    if not isinstance(place, Unset):
        json_place = place

    params["place"] = json_place

    params["view"] = view

    json_in_watchlist: str | Unset = UNSET
    if not isinstance(in_watchlist, Unset):
        json_in_watchlist = str(in_watchlist)
    params["inWatchlist"] = json_in_watchlist

    params["count"] = count

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
        "url": "/v1/records",
        "params": params,
    }

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | CountResult | RecordPage | Problem | None:
    if response.status_code == 200:

        def _parse_response_200(data: object) -> CountResult | RecordPage:
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                response_200_type_0 = RecordPage.from_dict(data)

                return response_200_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            if not isinstance(data, dict):
                raise TypeError()
            response_200_type_1 = CountResult.from_dict(data)

            return response_200_type_1

        response_200 = _parse_response_200(response.json())

        return response_200

    if response.status_code == 304:
        response_304 = cast(Any, None)
        return response_304

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if response.status_code == 503:
        response_503 = Problem.from_dict(response.json())

        return response_503

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Any | CountResult | RecordPage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    until: datetime.datetime | Unset = UNSET,
    q: str | Unset = UNSET,
    include_expired: bool | Unset = False,
    format_: ListRecordsFormat | Unset = ListRecordsFormat.JSON,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 500,
    external_id: list[str] | Unset = UNSET,
    near: str | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    time_field: ListRecordsTimeField | Unset = ListRecordsTimeField.EFFECTIVE,
    order: ListRecordsOrder | Unset = ListRecordsOrder.OBSERVED,
    sort: ListRecordsSort | Unset = UNSET,
    as_of_seq: int | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
    include: list[ListRecordsIncludeItem] | Unset = UNSET,
    fields: str | Unset = UNSET,
    precision: int | Unset = 7,
    simplify: float | Unset = UNSET,
    place: list[str] | Unset = UNSET,
    view: str | Unset = UNSET,
    in_watchlist: UUID | Unset = UNSET,
    count: bool | Unset = False,
    country: list[str] | Unset = UNSET,
    admin1: list[str] | Unset = UNSET,
    county: list[str] | Unset = UNSET,
    licence_policy: list[ListRecordsLicencePolicyItem] | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Response[Any | CountResult | RecordPage | Problem]:
    """Query current records

     Current state from record_head ⋈ record_live (merged payload, live geometry); expired and tombstoned
    records are excluded unless requested, and `watermarkSeq` is read before the query. Mirror contract:
    (1) open SSE with `after = watermarkSeq`, (2) page the snapshot with `order=id` (the cursor pins
    `asOfSeq ?? watermarkSeq`; later updates arrive via `/v1/changes?after=<w>`), (3) apply an event
    only if its observedAt ≥ the stored one. `asOf=T` replays transaction time and needs a source or
    kind filter (400 ASOF_FILTER_REQUIRED, ASOF_BEYOND_RETENTION with `horizon`, ASOF_INVALID).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        since (datetime.datetime | Unset):
        until (datetime.datetime | Unset):
        q (str | Unset):
        include_expired (bool | Unset):  Default: False.
        format_ (ListRecordsFormat | Unset):  Default: ListRecordsFormat.JSON.
        cursor (str | Unset):
        limit (int | Unset):  Default: 500.
        external_id (list[str] | Unset):
        near (str | Unset):  Example: -96.80,32.78.
        radius_km (float | Unset):
        time_field (ListRecordsTimeField | Unset):  Default: ListRecordsTimeField.EFFECTIVE.
        order (ListRecordsOrder | Unset):  Default: ListRecordsOrder.OBSERVED.
        sort (ListRecordsSort | Unset):
        as_of_seq (int | Unset):
        as_of (datetime.datetime | Unset):
        include (list[ListRecordsIncludeItem] | Unset):
        fields (str | Unset):  Example: externalId,title,geometry,payload.mag.
        precision (int | Unset):  Default: 7.
        simplify (float | Unset):
        place (list[str] | Unset):
        view (str | Unset):
        in_watchlist (UUID | Unset):
        count (bool | Unset):  Default: False.
        country (list[str] | Unset):
        admin1 (list[str] | Unset):
        county (list[str] | Unset):
        licence_policy (list[ListRecordsLicencePolicyItem] | Unset):
        if_none_match (str | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | CountResult | RecordPage | Problem]
    """

    kwargs = _get_kwargs(
        source=source,
        kind=kind,
        bbox=bbox,
        since=since,
        until=until,
        q=q,
        include_expired=include_expired,
        format_=format_,
        cursor=cursor,
        limit=limit,
        external_id=external_id,
        near=near,
        radius_km=radius_km,
        time_field=time_field,
        order=order,
        sort=sort,
        as_of_seq=as_of_seq,
        as_of=as_of,
        include=include,
        fields=fields,
        precision=precision,
        simplify=simplify,
        place=place,
        view=view,
        in_watchlist=in_watchlist,
        count=count,
        country=country,
        admin1=admin1,
        county=county,
        licence_policy=licence_policy,
        if_none_match=if_none_match,
        foresignal_licence_policy=foresignal_licence_policy,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    until: datetime.datetime | Unset = UNSET,
    q: str | Unset = UNSET,
    include_expired: bool | Unset = False,
    format_: ListRecordsFormat | Unset = ListRecordsFormat.JSON,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 500,
    external_id: list[str] | Unset = UNSET,
    near: str | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    time_field: ListRecordsTimeField | Unset = ListRecordsTimeField.EFFECTIVE,
    order: ListRecordsOrder | Unset = ListRecordsOrder.OBSERVED,
    sort: ListRecordsSort | Unset = UNSET,
    as_of_seq: int | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
    include: list[ListRecordsIncludeItem] | Unset = UNSET,
    fields: str | Unset = UNSET,
    precision: int | Unset = 7,
    simplify: float | Unset = UNSET,
    place: list[str] | Unset = UNSET,
    view: str | Unset = UNSET,
    in_watchlist: UUID | Unset = UNSET,
    count: bool | Unset = False,
    country: list[str] | Unset = UNSET,
    admin1: list[str] | Unset = UNSET,
    county: list[str] | Unset = UNSET,
    licence_policy: list[ListRecordsLicencePolicyItem] | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Any | CountResult | RecordPage | Problem | None:
    """Query current records

     Current state from record_head ⋈ record_live (merged payload, live geometry); expired and tombstoned
    records are excluded unless requested, and `watermarkSeq` is read before the query. Mirror contract:
    (1) open SSE with `after = watermarkSeq`, (2) page the snapshot with `order=id` (the cursor pins
    `asOfSeq ?? watermarkSeq`; later updates arrive via `/v1/changes?after=<w>`), (3) apply an event
    only if its observedAt ≥ the stored one. `asOf=T` replays transaction time and needs a source or
    kind filter (400 ASOF_FILTER_REQUIRED, ASOF_BEYOND_RETENTION with `horizon`, ASOF_INVALID).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        since (datetime.datetime | Unset):
        until (datetime.datetime | Unset):
        q (str | Unset):
        include_expired (bool | Unset):  Default: False.
        format_ (ListRecordsFormat | Unset):  Default: ListRecordsFormat.JSON.
        cursor (str | Unset):
        limit (int | Unset):  Default: 500.
        external_id (list[str] | Unset):
        near (str | Unset):  Example: -96.80,32.78.
        radius_km (float | Unset):
        time_field (ListRecordsTimeField | Unset):  Default: ListRecordsTimeField.EFFECTIVE.
        order (ListRecordsOrder | Unset):  Default: ListRecordsOrder.OBSERVED.
        sort (ListRecordsSort | Unset):
        as_of_seq (int | Unset):
        as_of (datetime.datetime | Unset):
        include (list[ListRecordsIncludeItem] | Unset):
        fields (str | Unset):  Example: externalId,title,geometry,payload.mag.
        precision (int | Unset):  Default: 7.
        simplify (float | Unset):
        place (list[str] | Unset):
        view (str | Unset):
        in_watchlist (UUID | Unset):
        count (bool | Unset):  Default: False.
        country (list[str] | Unset):
        admin1 (list[str] | Unset):
        county (list[str] | Unset):
        licence_policy (list[ListRecordsLicencePolicyItem] | Unset):
        if_none_match (str | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | CountResult | RecordPage | Problem
    """

    return sync_detailed(
        client=client,
        source=source,
        kind=kind,
        bbox=bbox,
        since=since,
        until=until,
        q=q,
        include_expired=include_expired,
        format_=format_,
        cursor=cursor,
        limit=limit,
        external_id=external_id,
        near=near,
        radius_km=radius_km,
        time_field=time_field,
        order=order,
        sort=sort,
        as_of_seq=as_of_seq,
        as_of=as_of,
        include=include,
        fields=fields,
        precision=precision,
        simplify=simplify,
        place=place,
        view=view,
        in_watchlist=in_watchlist,
        count=count,
        country=country,
        admin1=admin1,
        county=county,
        licence_policy=licence_policy,
        if_none_match=if_none_match,
        foresignal_licence_policy=foresignal_licence_policy,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    until: datetime.datetime | Unset = UNSET,
    q: str | Unset = UNSET,
    include_expired: bool | Unset = False,
    format_: ListRecordsFormat | Unset = ListRecordsFormat.JSON,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 500,
    external_id: list[str] | Unset = UNSET,
    near: str | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    time_field: ListRecordsTimeField | Unset = ListRecordsTimeField.EFFECTIVE,
    order: ListRecordsOrder | Unset = ListRecordsOrder.OBSERVED,
    sort: ListRecordsSort | Unset = UNSET,
    as_of_seq: int | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
    include: list[ListRecordsIncludeItem] | Unset = UNSET,
    fields: str | Unset = UNSET,
    precision: int | Unset = 7,
    simplify: float | Unset = UNSET,
    place: list[str] | Unset = UNSET,
    view: str | Unset = UNSET,
    in_watchlist: UUID | Unset = UNSET,
    count: bool | Unset = False,
    country: list[str] | Unset = UNSET,
    admin1: list[str] | Unset = UNSET,
    county: list[str] | Unset = UNSET,
    licence_policy: list[ListRecordsLicencePolicyItem] | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Response[Any | CountResult | RecordPage | Problem]:
    """Query current records

     Current state from record_head ⋈ record_live (merged payload, live geometry); expired and tombstoned
    records are excluded unless requested, and `watermarkSeq` is read before the query. Mirror contract:
    (1) open SSE with `after = watermarkSeq`, (2) page the snapshot with `order=id` (the cursor pins
    `asOfSeq ?? watermarkSeq`; later updates arrive via `/v1/changes?after=<w>`), (3) apply an event
    only if its observedAt ≥ the stored one. `asOf=T` replays transaction time and needs a source or
    kind filter (400 ASOF_FILTER_REQUIRED, ASOF_BEYOND_RETENTION with `horizon`, ASOF_INVALID).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        since (datetime.datetime | Unset):
        until (datetime.datetime | Unset):
        q (str | Unset):
        include_expired (bool | Unset):  Default: False.
        format_ (ListRecordsFormat | Unset):  Default: ListRecordsFormat.JSON.
        cursor (str | Unset):
        limit (int | Unset):  Default: 500.
        external_id (list[str] | Unset):
        near (str | Unset):  Example: -96.80,32.78.
        radius_km (float | Unset):
        time_field (ListRecordsTimeField | Unset):  Default: ListRecordsTimeField.EFFECTIVE.
        order (ListRecordsOrder | Unset):  Default: ListRecordsOrder.OBSERVED.
        sort (ListRecordsSort | Unset):
        as_of_seq (int | Unset):
        as_of (datetime.datetime | Unset):
        include (list[ListRecordsIncludeItem] | Unset):
        fields (str | Unset):  Example: externalId,title,geometry,payload.mag.
        precision (int | Unset):  Default: 7.
        simplify (float | Unset):
        place (list[str] | Unset):
        view (str | Unset):
        in_watchlist (UUID | Unset):
        count (bool | Unset):  Default: False.
        country (list[str] | Unset):
        admin1 (list[str] | Unset):
        county (list[str] | Unset):
        licence_policy (list[ListRecordsLicencePolicyItem] | Unset):
        if_none_match (str | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | CountResult | RecordPage | Problem]
    """

    kwargs = _get_kwargs(
        source=source,
        kind=kind,
        bbox=bbox,
        since=since,
        until=until,
        q=q,
        include_expired=include_expired,
        format_=format_,
        cursor=cursor,
        limit=limit,
        external_id=external_id,
        near=near,
        radius_km=radius_km,
        time_field=time_field,
        order=order,
        sort=sort,
        as_of_seq=as_of_seq,
        as_of=as_of,
        include=include,
        fields=fields,
        precision=precision,
        simplify=simplify,
        place=place,
        view=view,
        in_watchlist=in_watchlist,
        count=count,
        country=country,
        admin1=admin1,
        county=county,
        licence_policy=licence_policy,
        if_none_match=if_none_match,
        foresignal_licence_policy=foresignal_licence_policy,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    until: datetime.datetime | Unset = UNSET,
    q: str | Unset = UNSET,
    include_expired: bool | Unset = False,
    format_: ListRecordsFormat | Unset = ListRecordsFormat.JSON,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 500,
    external_id: list[str] | Unset = UNSET,
    near: str | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    time_field: ListRecordsTimeField | Unset = ListRecordsTimeField.EFFECTIVE,
    order: ListRecordsOrder | Unset = ListRecordsOrder.OBSERVED,
    sort: ListRecordsSort | Unset = UNSET,
    as_of_seq: int | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
    include: list[ListRecordsIncludeItem] | Unset = UNSET,
    fields: str | Unset = UNSET,
    precision: int | Unset = 7,
    simplify: float | Unset = UNSET,
    place: list[str] | Unset = UNSET,
    view: str | Unset = UNSET,
    in_watchlist: UUID | Unset = UNSET,
    count: bool | Unset = False,
    country: list[str] | Unset = UNSET,
    admin1: list[str] | Unset = UNSET,
    county: list[str] | Unset = UNSET,
    licence_policy: list[ListRecordsLicencePolicyItem] | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Any | CountResult | RecordPage | Problem | None:
    """Query current records

     Current state from record_head ⋈ record_live (merged payload, live geometry); expired and tombstoned
    records are excluded unless requested, and `watermarkSeq` is read before the query. Mirror contract:
    (1) open SSE with `after = watermarkSeq`, (2) page the snapshot with `order=id` (the cursor pins
    `asOfSeq ?? watermarkSeq`; later updates arrive via `/v1/changes?after=<w>`), (3) apply an event
    only if its observedAt ≥ the stored one. `asOf=T` replays transaction time and needs a source or
    kind filter (400 ASOF_FILTER_REQUIRED, ASOF_BEYOND_RETENTION with `horizon`, ASOF_INVALID).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        since (datetime.datetime | Unset):
        until (datetime.datetime | Unset):
        q (str | Unset):
        include_expired (bool | Unset):  Default: False.
        format_ (ListRecordsFormat | Unset):  Default: ListRecordsFormat.JSON.
        cursor (str | Unset):
        limit (int | Unset):  Default: 500.
        external_id (list[str] | Unset):
        near (str | Unset):  Example: -96.80,32.78.
        radius_km (float | Unset):
        time_field (ListRecordsTimeField | Unset):  Default: ListRecordsTimeField.EFFECTIVE.
        order (ListRecordsOrder | Unset):  Default: ListRecordsOrder.OBSERVED.
        sort (ListRecordsSort | Unset):
        as_of_seq (int | Unset):
        as_of (datetime.datetime | Unset):
        include (list[ListRecordsIncludeItem] | Unset):
        fields (str | Unset):  Example: externalId,title,geometry,payload.mag.
        precision (int | Unset):  Default: 7.
        simplify (float | Unset):
        place (list[str] | Unset):
        view (str | Unset):
        in_watchlist (UUID | Unset):
        count (bool | Unset):  Default: False.
        country (list[str] | Unset):
        admin1 (list[str] | Unset):
        county (list[str] | Unset):
        licence_policy (list[ListRecordsLicencePolicyItem] | Unset):
        if_none_match (str | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | CountResult | RecordPage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            source=source,
            kind=kind,
            bbox=bbox,
            since=since,
            until=until,
            q=q,
            include_expired=include_expired,
            format_=format_,
            cursor=cursor,
            limit=limit,
            external_id=external_id,
            near=near,
            radius_km=radius_km,
            time_field=time_field,
            order=order,
            sort=sort,
            as_of_seq=as_of_seq,
            as_of=as_of,
            include=include,
            fields=fields,
            precision=precision,
            simplify=simplify,
            place=place,
            view=view,
            in_watchlist=in_watchlist,
            count=count,
            country=country,
            admin1=admin1,
            county=county,
            licence_policy=licence_policy,
            if_none_match=if_none_match,
            foresignal_licence_policy=foresignal_licence_policy,
        )
    ).parsed
