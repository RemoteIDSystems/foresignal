from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.evidence_item import EvidenceItem
from ...models.evidence_item_patch import EvidenceItemPatch
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    evidence_item_id: str,
    *,
    body: EvidenceItemPatch,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "patch",
        "url": "/v1/evidence-items/{evidence_item_id}".format(
            evidence_item_id=quote(str(evidence_item_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/merge-patch+json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> EvidenceItem | Problem | None:
    if response.status_code == 200:
        response_200 = EvidenceItem.from_dict(response.json())

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

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[EvidenceItem | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    evidence_item_id: str,
    *,
    client: AuthenticatedClient,
    body: EvidenceItemPatch,
) -> Response[EvidenceItem | Problem]:
    """Update an evidence item

     JSON merge patch (owner or editor).

    **Scope:** `read`.

    Args:
        evidence_item_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (EvidenceItemPatch): JSON merge patch for an evidence item (owner or editor)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[EvidenceItem | Problem]
    """

    kwargs = _get_kwargs(
        evidence_item_id=evidence_item_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    evidence_item_id: str,
    *,
    client: AuthenticatedClient,
    body: EvidenceItemPatch,
) -> EvidenceItem | Problem | None:
    """Update an evidence item

     JSON merge patch (owner or editor).

    **Scope:** `read`.

    Args:
        evidence_item_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (EvidenceItemPatch): JSON merge patch for an evidence item (owner or editor)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        EvidenceItem | Problem
    """

    return sync_detailed(
        evidence_item_id=evidence_item_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    evidence_item_id: str,
    *,
    client: AuthenticatedClient,
    body: EvidenceItemPatch,
) -> Response[EvidenceItem | Problem]:
    """Update an evidence item

     JSON merge patch (owner or editor).

    **Scope:** `read`.

    Args:
        evidence_item_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (EvidenceItemPatch): JSON merge patch for an evidence item (owner or editor)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[EvidenceItem | Problem]
    """

    kwargs = _get_kwargs(
        evidence_item_id=evidence_item_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    evidence_item_id: str,
    *,
    client: AuthenticatedClient,
    body: EvidenceItemPatch,
) -> EvidenceItem | Problem | None:
    """Update an evidence item

     JSON merge patch (owner or editor).

    **Scope:** `read`.

    Args:
        evidence_item_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (EvidenceItemPatch): JSON merge patch for an evidence item (owner or editor)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        EvidenceItem | Problem
    """

    return (
        await asyncio_detailed(
            evidence_item_id=evidence_item_id,
            client=client,
            body=body,
        )
    ).parsed
