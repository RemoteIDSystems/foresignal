from http import HTTPStatus
from typing import Any
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.stream_changes_licence_policy_item import StreamChangesLicencePolicyItem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    after: int | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    payload: bool | Unset = False,
    external_id: list[str] | Unset = UNSET,
    near: str | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    live: bool | Unset = True,
    touches: bool | Unset = False,
    ticket: str | Unset = UNSET,
    place: list[str] | Unset = UNSET,
    view: str | Unset = UNSET,
    in_watchlist: UUID | Unset = UNSET,
    licence_policy: list[StreamChangesLicencePolicyItem] | Unset = UNSET,
    last_event_id: str | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(last_event_id, Unset):
        headers["Last-Event-ID"] = last_event_id

    if not isinstance(foresignal_licence_policy, Unset):
        headers["Foresignal-Licence-Policy"] = foresignal_licence_policy

    params: dict[str, Any] = {}

    params["after"] = after

    json_source: list[str] | Unset = UNSET
    if not isinstance(source, Unset):
        json_source = source

    params["source"] = json_source

    json_kind: list[str] | Unset = UNSET
    if not isinstance(kind, Unset):
        json_kind = kind

    params["kind"] = json_kind

    params["bbox"] = bbox

    params["payload"] = payload

    json_external_id: list[str] | Unset = UNSET
    if not isinstance(external_id, Unset):
        json_external_id = external_id

    params["externalId"] = json_external_id

    params["near"] = near

    params["radiusKm"] = radius_km

    params["live"] = live

    params["touches"] = touches

    params["ticket"] = ticket

    json_place: list[str] | Unset = UNSET
    if not isinstance(place, Unset):
        json_place = place

    params["place"] = json_place

    params["view"] = view

    json_in_watchlist: str | Unset = UNSET
    if not isinstance(in_watchlist, Unset):
        json_in_watchlist = str(in_watchlist)
    params["inWatchlist"] = json_in_watchlist

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
        "url": "/v1/changes/stream",
        "params": params,
    }

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | None:
    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

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
) -> Response[Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    after: int | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    payload: bool | Unset = False,
    external_id: list[str] | Unset = UNSET,
    near: str | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    live: bool | Unset = True,
    touches: bool | Unset = False,
    ticket: str | Unset = UNSET,
    place: list[str] | Unset = UNSET,
    view: str | Unset = UNSET,
    in_watchlist: UUID | Unset = UNSET,
    licence_policy: list[StreamChangesLicencePolicyItem] | Unset = UNSET,
    last_event_id: str | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Response[Problem]:
    """Live change stream (SSE)

     Two lanes: lossless `change` events (`id:` = seq; replayed from `Last-Event-ID`, else `after`, else
    live from now) and conflated, non-persisted `live`/`touch` updates without ids. A spatial filter
    matches bbox, prevPersistedBbox or prevLiveBbox; when only a previous box matches, `leftFilter:
    true` tells the client to remove the feature. On `resync` re-fetch current state
    (`/v1/records?order=id` or tiles) and keep the stream open; on `reset` reconnect from `resumeAfter`,
    or re-snapshot when the reason is `resync_required`; apply an event only if its observedAt ≥ the
    stored one.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        after (int | Unset):
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        payload (bool | Unset):  Default: False.
        external_id (list[str] | Unset):
        near (str | Unset):  Example: -96.80,32.78.
        radius_km (float | Unset):
        live (bool | Unset):  Default: True.
        touches (bool | Unset):  Default: False.
        ticket (str | Unset):
        place (list[str] | Unset):
        view (str | Unset):
        in_watchlist (UUID | Unset):
        licence_policy (list[StreamChangesLicencePolicyItem] | Unset):
        last_event_id (str | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
    """

    kwargs = _get_kwargs(
        after=after,
        source=source,
        kind=kind,
        bbox=bbox,
        payload=payload,
        external_id=external_id,
        near=near,
        radius_km=radius_km,
        live=live,
        touches=touches,
        ticket=ticket,
        place=place,
        view=view,
        in_watchlist=in_watchlist,
        licence_policy=licence_policy,
        last_event_id=last_event_id,
        foresignal_licence_policy=foresignal_licence_policy,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    after: int | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    payload: bool | Unset = False,
    external_id: list[str] | Unset = UNSET,
    near: str | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    live: bool | Unset = True,
    touches: bool | Unset = False,
    ticket: str | Unset = UNSET,
    place: list[str] | Unset = UNSET,
    view: str | Unset = UNSET,
    in_watchlist: UUID | Unset = UNSET,
    licence_policy: list[StreamChangesLicencePolicyItem] | Unset = UNSET,
    last_event_id: str | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Problem | None:
    """Live change stream (SSE)

     Two lanes: lossless `change` events (`id:` = seq; replayed from `Last-Event-ID`, else `after`, else
    live from now) and conflated, non-persisted `live`/`touch` updates without ids. A spatial filter
    matches bbox, prevPersistedBbox or prevLiveBbox; when only a previous box matches, `leftFilter:
    true` tells the client to remove the feature. On `resync` re-fetch current state
    (`/v1/records?order=id` or tiles) and keep the stream open; on `reset` reconnect from `resumeAfter`,
    or re-snapshot when the reason is `resync_required`; apply an event only if its observedAt ≥ the
    stored one.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        after (int | Unset):
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        payload (bool | Unset):  Default: False.
        external_id (list[str] | Unset):
        near (str | Unset):  Example: -96.80,32.78.
        radius_km (float | Unset):
        live (bool | Unset):  Default: True.
        touches (bool | Unset):  Default: False.
        ticket (str | Unset):
        place (list[str] | Unset):
        view (str | Unset):
        in_watchlist (UUID | Unset):
        licence_policy (list[StreamChangesLicencePolicyItem] | Unset):
        last_event_id (str | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return sync_detailed(
        client=client,
        after=after,
        source=source,
        kind=kind,
        bbox=bbox,
        payload=payload,
        external_id=external_id,
        near=near,
        radius_km=radius_km,
        live=live,
        touches=touches,
        ticket=ticket,
        place=place,
        view=view,
        in_watchlist=in_watchlist,
        licence_policy=licence_policy,
        last_event_id=last_event_id,
        foresignal_licence_policy=foresignal_licence_policy,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    after: int | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    payload: bool | Unset = False,
    external_id: list[str] | Unset = UNSET,
    near: str | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    live: bool | Unset = True,
    touches: bool | Unset = False,
    ticket: str | Unset = UNSET,
    place: list[str] | Unset = UNSET,
    view: str | Unset = UNSET,
    in_watchlist: UUID | Unset = UNSET,
    licence_policy: list[StreamChangesLicencePolicyItem] | Unset = UNSET,
    last_event_id: str | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Response[Problem]:
    """Live change stream (SSE)

     Two lanes: lossless `change` events (`id:` = seq; replayed from `Last-Event-ID`, else `after`, else
    live from now) and conflated, non-persisted `live`/`touch` updates without ids. A spatial filter
    matches bbox, prevPersistedBbox or prevLiveBbox; when only a previous box matches, `leftFilter:
    true` tells the client to remove the feature. On `resync` re-fetch current state
    (`/v1/records?order=id` or tiles) and keep the stream open; on `reset` reconnect from `resumeAfter`,
    or re-snapshot when the reason is `resync_required`; apply an event only if its observedAt ≥ the
    stored one.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        after (int | Unset):
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        payload (bool | Unset):  Default: False.
        external_id (list[str] | Unset):
        near (str | Unset):  Example: -96.80,32.78.
        radius_km (float | Unset):
        live (bool | Unset):  Default: True.
        touches (bool | Unset):  Default: False.
        ticket (str | Unset):
        place (list[str] | Unset):
        view (str | Unset):
        in_watchlist (UUID | Unset):
        licence_policy (list[StreamChangesLicencePolicyItem] | Unset):
        last_event_id (str | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
    """

    kwargs = _get_kwargs(
        after=after,
        source=source,
        kind=kind,
        bbox=bbox,
        payload=payload,
        external_id=external_id,
        near=near,
        radius_km=radius_km,
        live=live,
        touches=touches,
        ticket=ticket,
        place=place,
        view=view,
        in_watchlist=in_watchlist,
        licence_policy=licence_policy,
        last_event_id=last_event_id,
        foresignal_licence_policy=foresignal_licence_policy,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    after: int | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    payload: bool | Unset = False,
    external_id: list[str] | Unset = UNSET,
    near: str | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    live: bool | Unset = True,
    touches: bool | Unset = False,
    ticket: str | Unset = UNSET,
    place: list[str] | Unset = UNSET,
    view: str | Unset = UNSET,
    in_watchlist: UUID | Unset = UNSET,
    licence_policy: list[StreamChangesLicencePolicyItem] | Unset = UNSET,
    last_event_id: str | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Problem | None:
    """Live change stream (SSE)

     Two lanes: lossless `change` events (`id:` = seq; replayed from `Last-Event-ID`, else `after`, else
    live from now) and conflated, non-persisted `live`/`touch` updates without ids. A spatial filter
    matches bbox, prevPersistedBbox or prevLiveBbox; when only a previous box matches, `leftFilter:
    true` tells the client to remove the feature. On `resync` re-fetch current state
    (`/v1/records?order=id` or tiles) and keep the stream open; on `reset` reconnect from `resumeAfter`,
    or re-snapshot when the reason is `resync_required`; apply an event only if its observedAt ≥ the
    stored one.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        after (int | Unset):
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        payload (bool | Unset):  Default: False.
        external_id (list[str] | Unset):
        near (str | Unset):  Example: -96.80,32.78.
        radius_km (float | Unset):
        live (bool | Unset):  Default: True.
        touches (bool | Unset):  Default: False.
        ticket (str | Unset):
        place (list[str] | Unset):
        view (str | Unset):
        in_watchlist (UUID | Unset):
        licence_policy (list[StreamChangesLicencePolicyItem] | Unset):
        last_event_id (str | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            after=after,
            source=source,
            kind=kind,
            bbox=bbox,
            payload=payload,
            external_id=external_id,
            near=near,
            radius_km=radius_km,
            live=live,
            touches=touches,
            ticket=ticket,
            place=place,
            view=view,
            in_watchlist=in_watchlist,
            licence_policy=licence_policy,
            last_event_id=last_event_id,
            foresignal_licence_policy=foresignal_licence_policy,
        )
    ).parsed
