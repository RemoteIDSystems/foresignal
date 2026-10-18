import datetime
from http import HTTPStatus
from typing import Any
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.count_result import CountResult
from ...models.history_page import HistoryPage
from ...models.problem import Problem
from ...models.query_history_format import QueryHistoryFormat
from ...models.query_history_licence_policy_item import QueryHistoryLicencePolicyItem
from ...models.query_history_time_field import QueryHistoryTimeField
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    since: datetime.datetime,
    until: datetime.datetime | Unset = UNSET,
    format_: QueryHistoryFormat | Unset = QueryHistoryFormat.JSON,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 5000,
    time_field: QueryHistoryTimeField | Unset = QueryHistoryTimeField.OBSERVED,
    external_id: list[str] | Unset = UNSET,
    near: str | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    fields: str | Unset = UNSET,
    precision: int | Unset = 7,
    simplify: float | Unset = UNSET,
    place: list[str] | Unset = UNSET,
    view: str | Unset = UNSET,
    in_watchlist: UUID | Unset = UNSET,
    count: bool | Unset = False,
    licence_policy: list[QueryHistoryLicencePolicyItem] | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
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

    json_since = since.isoformat()
    params["since"] = json_since

    json_until: str | Unset = UNSET
    if not isinstance(until, Unset):
        json_until = until.isoformat()
    params["until"] = json_until

    json_format_: str | Unset = UNSET
    if not isinstance(format_, Unset):
        json_format_ = format_.value

    params["format"] = json_format_

    params["cursor"] = cursor

    params["limit"] = limit

    json_time_field: str | Unset = UNSET
    if not isinstance(time_field, Unset):
        json_time_field = time_field.value

    params["timeField"] = json_time_field

    json_external_id: list[str] | Unset = UNSET
    if not isinstance(external_id, Unset):
        json_external_id = external_id

    params["externalId"] = json_external_id

    params["near"] = near

    params["radiusKm"] = radius_km

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
        "url": "/v1/history",
        "params": params,
    }

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> CountResult | HistoryPage | Problem | None:
    if response.status_code == 200:

        def _parse_response_200(data: object) -> CountResult | HistoryPage:
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                response_200_type_0 = HistoryPage.from_dict(data)

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
) -> Response[CountResult | HistoryPage | Problem]:
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
    since: datetime.datetime,
    until: datetime.datetime | Unset = UNSET,
    format_: QueryHistoryFormat | Unset = QueryHistoryFormat.JSON,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 5000,
    time_field: QueryHistoryTimeField | Unset = QueryHistoryTimeField.OBSERVED,
    external_id: list[str] | Unset = UNSET,
    near: str | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    fields: str | Unset = UNSET,
    precision: int | Unset = 7,
    simplify: float | Unset = UNSET,
    place: list[str] | Unset = UNSET,
    view: str | Unset = UNSET,
    in_watchlist: UUID | Unset = UNSET,
    count: bool | Unset = False,
    licence_policy: list[QueryHistoryLicencePolicyItem] | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Response[CountResult | HistoryPage | Problem]:
    """All versions in a time range (playback)

     All versions in a range from record_version (playback), keyset on (observedAt, seq), ≤ 50,000 per
    page. `since` is required.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        since (datetime.datetime):
        until (datetime.datetime | Unset):
        format_ (QueryHistoryFormat | Unset):  Default: QueryHistoryFormat.JSON.
        cursor (str | Unset):
        limit (int | Unset):  Default: 5000.
        time_field (QueryHistoryTimeField | Unset):  Default: QueryHistoryTimeField.OBSERVED.
        external_id (list[str] | Unset):
        near (str | Unset):  Example: -96.80,32.78.
        radius_km (float | Unset):
        fields (str | Unset):  Example: externalId,title,geometry,payload.mag.
        precision (int | Unset):  Default: 7.
        simplify (float | Unset):
        place (list[str] | Unset):
        view (str | Unset):
        in_watchlist (UUID | Unset):
        count (bool | Unset):  Default: False.
        licence_policy (list[QueryHistoryLicencePolicyItem] | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[CountResult | HistoryPage | Problem]
    """

    kwargs = _get_kwargs(
        source=source,
        kind=kind,
        bbox=bbox,
        since=since,
        until=until,
        format_=format_,
        cursor=cursor,
        limit=limit,
        time_field=time_field,
        external_id=external_id,
        near=near,
        radius_km=radius_km,
        fields=fields,
        precision=precision,
        simplify=simplify,
        place=place,
        view=view,
        in_watchlist=in_watchlist,
        count=count,
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
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    since: datetime.datetime,
    until: datetime.datetime | Unset = UNSET,
    format_: QueryHistoryFormat | Unset = QueryHistoryFormat.JSON,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 5000,
    time_field: QueryHistoryTimeField | Unset = QueryHistoryTimeField.OBSERVED,
    external_id: list[str] | Unset = UNSET,
    near: str | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    fields: str | Unset = UNSET,
    precision: int | Unset = 7,
    simplify: float | Unset = UNSET,
    place: list[str] | Unset = UNSET,
    view: str | Unset = UNSET,
    in_watchlist: UUID | Unset = UNSET,
    count: bool | Unset = False,
    licence_policy: list[QueryHistoryLicencePolicyItem] | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> CountResult | HistoryPage | Problem | None:
    """All versions in a time range (playback)

     All versions in a range from record_version (playback), keyset on (observedAt, seq), ≤ 50,000 per
    page. `since` is required.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        since (datetime.datetime):
        until (datetime.datetime | Unset):
        format_ (QueryHistoryFormat | Unset):  Default: QueryHistoryFormat.JSON.
        cursor (str | Unset):
        limit (int | Unset):  Default: 5000.
        time_field (QueryHistoryTimeField | Unset):  Default: QueryHistoryTimeField.OBSERVED.
        external_id (list[str] | Unset):
        near (str | Unset):  Example: -96.80,32.78.
        radius_km (float | Unset):
        fields (str | Unset):  Example: externalId,title,geometry,payload.mag.
        precision (int | Unset):  Default: 7.
        simplify (float | Unset):
        place (list[str] | Unset):
        view (str | Unset):
        in_watchlist (UUID | Unset):
        count (bool | Unset):  Default: False.
        licence_policy (list[QueryHistoryLicencePolicyItem] | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        CountResult | HistoryPage | Problem
    """

    return sync_detailed(
        client=client,
        source=source,
        kind=kind,
        bbox=bbox,
        since=since,
        until=until,
        format_=format_,
        cursor=cursor,
        limit=limit,
        time_field=time_field,
        external_id=external_id,
        near=near,
        radius_km=radius_km,
        fields=fields,
        precision=precision,
        simplify=simplify,
        place=place,
        view=view,
        in_watchlist=in_watchlist,
        count=count,
        licence_policy=licence_policy,
        foresignal_licence_policy=foresignal_licence_policy,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    since: datetime.datetime,
    until: datetime.datetime | Unset = UNSET,
    format_: QueryHistoryFormat | Unset = QueryHistoryFormat.JSON,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 5000,
    time_field: QueryHistoryTimeField | Unset = QueryHistoryTimeField.OBSERVED,
    external_id: list[str] | Unset = UNSET,
    near: str | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    fields: str | Unset = UNSET,
    precision: int | Unset = 7,
    simplify: float | Unset = UNSET,
    place: list[str] | Unset = UNSET,
    view: str | Unset = UNSET,
    in_watchlist: UUID | Unset = UNSET,
    count: bool | Unset = False,
    licence_policy: list[QueryHistoryLicencePolicyItem] | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Response[CountResult | HistoryPage | Problem]:
    """All versions in a time range (playback)

     All versions in a range from record_version (playback), keyset on (observedAt, seq), ≤ 50,000 per
    page. `since` is required.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        since (datetime.datetime):
        until (datetime.datetime | Unset):
        format_ (QueryHistoryFormat | Unset):  Default: QueryHistoryFormat.JSON.
        cursor (str | Unset):
        limit (int | Unset):  Default: 5000.
        time_field (QueryHistoryTimeField | Unset):  Default: QueryHistoryTimeField.OBSERVED.
        external_id (list[str] | Unset):
        near (str | Unset):  Example: -96.80,32.78.
        radius_km (float | Unset):
        fields (str | Unset):  Example: externalId,title,geometry,payload.mag.
        precision (int | Unset):  Default: 7.
        simplify (float | Unset):
        place (list[str] | Unset):
        view (str | Unset):
        in_watchlist (UUID | Unset):
        count (bool | Unset):  Default: False.
        licence_policy (list[QueryHistoryLicencePolicyItem] | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[CountResult | HistoryPage | Problem]
    """

    kwargs = _get_kwargs(
        source=source,
        kind=kind,
        bbox=bbox,
        since=since,
        until=until,
        format_=format_,
        cursor=cursor,
        limit=limit,
        time_field=time_field,
        external_id=external_id,
        near=near,
        radius_km=radius_km,
        fields=fields,
        precision=precision,
        simplify=simplify,
        place=place,
        view=view,
        in_watchlist=in_watchlist,
        count=count,
        licence_policy=licence_policy,
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
    since: datetime.datetime,
    until: datetime.datetime | Unset = UNSET,
    format_: QueryHistoryFormat | Unset = QueryHistoryFormat.JSON,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 5000,
    time_field: QueryHistoryTimeField | Unset = QueryHistoryTimeField.OBSERVED,
    external_id: list[str] | Unset = UNSET,
    near: str | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    fields: str | Unset = UNSET,
    precision: int | Unset = 7,
    simplify: float | Unset = UNSET,
    place: list[str] | Unset = UNSET,
    view: str | Unset = UNSET,
    in_watchlist: UUID | Unset = UNSET,
    count: bool | Unset = False,
    licence_policy: list[QueryHistoryLicencePolicyItem] | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> CountResult | HistoryPage | Problem | None:
    """All versions in a time range (playback)

     All versions in a range from record_version (playback), keyset on (observedAt, seq), ≤ 50,000 per
    page. `since` is required.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        since (datetime.datetime):
        until (datetime.datetime | Unset):
        format_ (QueryHistoryFormat | Unset):  Default: QueryHistoryFormat.JSON.
        cursor (str | Unset):
        limit (int | Unset):  Default: 5000.
        time_field (QueryHistoryTimeField | Unset):  Default: QueryHistoryTimeField.OBSERVED.
        external_id (list[str] | Unset):
        near (str | Unset):  Example: -96.80,32.78.
        radius_km (float | Unset):
        fields (str | Unset):  Example: externalId,title,geometry,payload.mag.
        precision (int | Unset):  Default: 7.
        simplify (float | Unset):
        place (list[str] | Unset):
        view (str | Unset):
        in_watchlist (UUID | Unset):
        count (bool | Unset):  Default: False.
        licence_policy (list[QueryHistoryLicencePolicyItem] | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        CountResult | HistoryPage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            source=source,
            kind=kind,
            bbox=bbox,
            since=since,
            until=until,
            format_=format_,
            cursor=cursor,
            limit=limit,
            time_field=time_field,
            external_id=external_id,
            near=near,
            radius_km=radius_km,
            fields=fields,
            precision=precision,
            simplify=simplify,
            place=place,
            view=view,
            in_watchlist=in_watchlist,
            count=count,
            licence_policy=licence_policy,
            foresignal_licence_policy=foresignal_licence_policy,
        )
    ).parsed
