from http import HTTPStatus
from typing import Any, cast
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_alerts_atom_min_severity import GetAlertsAtomMinSeverity
from ...models.get_alerts_atom_state_item import GetAlertsAtomStateItem
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    rule: list[UUID] | Unset = UNSET,
    min_severity: GetAlertsAtomMinSeverity | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    state: list[GetAlertsAtomStateItem] | Unset = UNSET,
    limit: int | Unset = 50,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_rule: list[str] | Unset = UNSET
    if not isinstance(rule, Unset):
        json_rule = []
        for rule_item_data in rule:
            rule_item = str(rule_item_data)
            json_rule.append(rule_item)

    params["rule"] = json_rule

    json_min_severity: str | Unset = UNSET
    if not isinstance(min_severity, Unset):
        json_min_severity = min_severity.value

    params["minSeverity"] = json_min_severity

    json_source: list[str] | Unset = UNSET
    if not isinstance(source, Unset):
        json_source = source

    params["source"] = json_source

    json_kind: list[str] | Unset = UNSET
    if not isinstance(kind, Unset):
        json_kind = kind

    params["kind"] = json_kind

    json_state: list[str] | Unset = UNSET
    if not isinstance(state, Unset):
        json_state = []
        for state_item_data in state:
            state_item = state_item_data.value
            json_state.append(state_item)

    params["state"] = json_state

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/alerts/feed.atom",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | Problem | None:
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

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Any | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    rule: list[UUID] | Unset = UNSET,
    min_severity: GetAlertsAtomMinSeverity | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    state: list[GetAlertsAtomStateItem] | Unset = UNSET,
    limit: int | Unset = 50,
) -> Response[Any | Problem]:
    """Alerts as an Atom feed

     RFC 4287 Atom feed of alerts with GeoRSS-Simple geometry (`georss:point`, or `georss:polygon` for
    single polygons ≤ 500 vertices, else the centroid). Entries are ordered newest first by last
    lifecycle change. Entry `id` is `tag:foresignal,2026:{installId}/alert/{id}`, title is `shortTitle`
    (else the title), the alternate link is the client deep link (else `publicBaseUrl/v1/alerts/{id}`),
    categories carry severity and rule tags. Content is markdown rendered with commonmark-java and
    sanitised by an allowlist (`p, br, strong, em, code, pre, ul, ol, li, blockquote, h3–h6, a[href]`
    http/https only). Private-source alerts are excluded unless the token or feed key passes the private
    gate and `EgressPolicy.allows(FEED)`. Conditional GET: ETag over (max lifecycle timestamp, max id,
    filters) plus `Last-Modified`. Feed readers without headers use a 035 feed key
    (`/v1/k/{key}/alerts/feed.atom`). RSS 2.0 and WebSub publishing are not offered.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        rule (list[UUID] | Unset):
        min_severity (GetAlertsAtomMinSeverity | Unset):
        source (list[str] | Unset):
        kind (list[str] | Unset):
        state (list[GetAlertsAtomStateItem] | Unset):
        limit (int | Unset):  Default: 50.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem]
    """

    kwargs = _get_kwargs(
        rule=rule,
        min_severity=min_severity,
        source=source,
        kind=kind,
        state=state,
        limit=limit,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    rule: list[UUID] | Unset = UNSET,
    min_severity: GetAlertsAtomMinSeverity | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    state: list[GetAlertsAtomStateItem] | Unset = UNSET,
    limit: int | Unset = 50,
) -> Any | Problem | None:
    """Alerts as an Atom feed

     RFC 4287 Atom feed of alerts with GeoRSS-Simple geometry (`georss:point`, or `georss:polygon` for
    single polygons ≤ 500 vertices, else the centroid). Entries are ordered newest first by last
    lifecycle change. Entry `id` is `tag:foresignal,2026:{installId}/alert/{id}`, title is `shortTitle`
    (else the title), the alternate link is the client deep link (else `publicBaseUrl/v1/alerts/{id}`),
    categories carry severity and rule tags. Content is markdown rendered with commonmark-java and
    sanitised by an allowlist (`p, br, strong, em, code, pre, ul, ol, li, blockquote, h3–h6, a[href]`
    http/https only). Private-source alerts are excluded unless the token or feed key passes the private
    gate and `EgressPolicy.allows(FEED)`. Conditional GET: ETag over (max lifecycle timestamp, max id,
    filters) plus `Last-Modified`. Feed readers without headers use a 035 feed key
    (`/v1/k/{key}/alerts/feed.atom`). RSS 2.0 and WebSub publishing are not offered.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        rule (list[UUID] | Unset):
        min_severity (GetAlertsAtomMinSeverity | Unset):
        source (list[str] | Unset):
        kind (list[str] | Unset):
        state (list[GetAlertsAtomStateItem] | Unset):
        limit (int | Unset):  Default: 50.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem
    """

    return sync_detailed(
        client=client,
        rule=rule,
        min_severity=min_severity,
        source=source,
        kind=kind,
        state=state,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    rule: list[UUID] | Unset = UNSET,
    min_severity: GetAlertsAtomMinSeverity | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    state: list[GetAlertsAtomStateItem] | Unset = UNSET,
    limit: int | Unset = 50,
) -> Response[Any | Problem]:
    """Alerts as an Atom feed

     RFC 4287 Atom feed of alerts with GeoRSS-Simple geometry (`georss:point`, or `georss:polygon` for
    single polygons ≤ 500 vertices, else the centroid). Entries are ordered newest first by last
    lifecycle change. Entry `id` is `tag:foresignal,2026:{installId}/alert/{id}`, title is `shortTitle`
    (else the title), the alternate link is the client deep link (else `publicBaseUrl/v1/alerts/{id}`),
    categories carry severity and rule tags. Content is markdown rendered with commonmark-java and
    sanitised by an allowlist (`p, br, strong, em, code, pre, ul, ol, li, blockquote, h3–h6, a[href]`
    http/https only). Private-source alerts are excluded unless the token or feed key passes the private
    gate and `EgressPolicy.allows(FEED)`. Conditional GET: ETag over (max lifecycle timestamp, max id,
    filters) plus `Last-Modified`. Feed readers without headers use a 035 feed key
    (`/v1/k/{key}/alerts/feed.atom`). RSS 2.0 and WebSub publishing are not offered.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        rule (list[UUID] | Unset):
        min_severity (GetAlertsAtomMinSeverity | Unset):
        source (list[str] | Unset):
        kind (list[str] | Unset):
        state (list[GetAlertsAtomStateItem] | Unset):
        limit (int | Unset):  Default: 50.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem]
    """

    kwargs = _get_kwargs(
        rule=rule,
        min_severity=min_severity,
        source=source,
        kind=kind,
        state=state,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    rule: list[UUID] | Unset = UNSET,
    min_severity: GetAlertsAtomMinSeverity | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    state: list[GetAlertsAtomStateItem] | Unset = UNSET,
    limit: int | Unset = 50,
) -> Any | Problem | None:
    """Alerts as an Atom feed

     RFC 4287 Atom feed of alerts with GeoRSS-Simple geometry (`georss:point`, or `georss:polygon` for
    single polygons ≤ 500 vertices, else the centroid). Entries are ordered newest first by last
    lifecycle change. Entry `id` is `tag:foresignal,2026:{installId}/alert/{id}`, title is `shortTitle`
    (else the title), the alternate link is the client deep link (else `publicBaseUrl/v1/alerts/{id}`),
    categories carry severity and rule tags. Content is markdown rendered with commonmark-java and
    sanitised by an allowlist (`p, br, strong, em, code, pre, ul, ol, li, blockquote, h3–h6, a[href]`
    http/https only). Private-source alerts are excluded unless the token or feed key passes the private
    gate and `EgressPolicy.allows(FEED)`. Conditional GET: ETag over (max lifecycle timestamp, max id,
    filters) plus `Last-Modified`. Feed readers without headers use a 035 feed key
    (`/v1/k/{key}/alerts/feed.atom`). RSS 2.0 and WebSub publishing are not offered.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        rule (list[UUID] | Unset):
        min_severity (GetAlertsAtomMinSeverity | Unset):
        source (list[str] | Unset):
        kind (list[str] | Unset):
        state (list[GetAlertsAtomStateItem] | Unset):
        limit (int | Unset):  Default: 50.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            rule=rule,
            min_severity=min_severity,
            source=source,
            kind=kind,
            state=state,
            limit=limit,
        )
    ).parsed
