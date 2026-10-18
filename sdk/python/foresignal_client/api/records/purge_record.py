from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.purge_report import PurgeReport
from ...models.purge_request import PurgeRequest
from ...types import UNSET, Response, Unset


def _get_kwargs(
    source_id: str,
    external_id: str,
    *,
    body: PurgeRequest | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/records/{source_id}/{external_id}/purge".format(
            source_id=quote(str(source_id), safe=""),
            external_id=quote(str(external_id), safe=""),
        ),
    }

    if not isinstance(body, Unset):
        _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | PurgeReport | None:
    if response.status_code == 200:
        response_200 = PurgeReport.from_dict(response.json())

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
) -> Response[Problem | PurgeReport]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    source_id: str,
    external_id: str,
    *,
    client: AuthenticatedClient,
    body: PurgeRequest | Unset = UNSET,
) -> Response[Problem | PurgeReport]:
    """Erase one entity

     Submits an `EntityPurge` writer command: writes a tombstone (qa flag PURGED; its change carries
    `purged: true`), deletes every older version on both tiers, then fans out: embeddings deleted, alert
    snapshots and messages redacted (or alerts deleted with `purgeAlerts`), notification bodies
    redacted, region rows and metric series dropped; Ask messages and briefing runs citing it are
    counted. Audited.

    **Scope:** `admin`.

    Args:
        source_id (str):
        external_id (str):
        body (PurgeRequest | Unset): Options for an entity purge (039)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | PurgeReport]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        external_id=external_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    source_id: str,
    external_id: str,
    *,
    client: AuthenticatedClient,
    body: PurgeRequest | Unset = UNSET,
) -> Problem | PurgeReport | None:
    """Erase one entity

     Submits an `EntityPurge` writer command: writes a tombstone (qa flag PURGED; its change carries
    `purged: true`), deletes every older version on both tiers, then fans out: embeddings deleted, alert
    snapshots and messages redacted (or alerts deleted with `purgeAlerts`), notification bodies
    redacted, region rows and metric series dropped; Ask messages and briefing runs citing it are
    counted. Audited.

    **Scope:** `admin`.

    Args:
        source_id (str):
        external_id (str):
        body (PurgeRequest | Unset): Options for an entity purge (039)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | PurgeReport
    """

    return sync_detailed(
        source_id=source_id,
        external_id=external_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    source_id: str,
    external_id: str,
    *,
    client: AuthenticatedClient,
    body: PurgeRequest | Unset = UNSET,
) -> Response[Problem | PurgeReport]:
    """Erase one entity

     Submits an `EntityPurge` writer command: writes a tombstone (qa flag PURGED; its change carries
    `purged: true`), deletes every older version on both tiers, then fans out: embeddings deleted, alert
    snapshots and messages redacted (or alerts deleted with `purgeAlerts`), notification bodies
    redacted, region rows and metric series dropped; Ask messages and briefing runs citing it are
    counted. Audited.

    **Scope:** `admin`.

    Args:
        source_id (str):
        external_id (str):
        body (PurgeRequest | Unset): Options for an entity purge (039)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | PurgeReport]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        external_id=external_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    source_id: str,
    external_id: str,
    *,
    client: AuthenticatedClient,
    body: PurgeRequest | Unset = UNSET,
) -> Problem | PurgeReport | None:
    """Erase one entity

     Submits an `EntityPurge` writer command: writes a tombstone (qa flag PURGED; its change carries
    `purged: true`), deletes every older version on both tiers, then fans out: embeddings deleted, alert
    snapshots and messages redacted (or alerts deleted with `purgeAlerts`), notification bodies
    redacted, region rows and metric series dropped; Ask messages and briefing runs citing it are
    counted. Audited.

    **Scope:** `admin`.

    Args:
        source_id (str):
        external_id (str):
        body (PurgeRequest | Unset): Options for an entity purge (039)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | PurgeReport
    """

    return (
        await asyncio_detailed(
            source_id=source_id,
            external_id=external_id,
            client=client,
            body=body,
        )
    ).parsed
