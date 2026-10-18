from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.investigation_item import InvestigationItem
from ...models.investigation_item_input import InvestigationItemInput
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    investigation_id: str,
    *,
    body: InvestigationItemInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/investigations/{investigation_id}/items".format(
            investigation_id=quote(str(investigation_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> InvestigationItem | Problem | None:
    if response.status_code == 201:
        response_201 = InvestigationItem.from_dict(response.json())

        return response_201

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

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[InvestigationItem | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    investigation_id: str,
    *,
    client: AuthenticatedClient,
    body: InvestigationItemInput,
) -> Response[InvestigationItem | Problem]:
    """Add an item to an investigation

     Pins the item with a snapshot (≤ 64 KB) so it survives retention. Adding an alert sets its 017 state
    to `investigating` when that state exists.

    **Scope:** `agent` or `admin`.

    Args:
        investigation_id (str):
        body (InvestigationItemInput): Item to add; adding an alert sets its 017 state to
            `investigating` when that state exists; 130: evidence items, attachments and any object
            reference, snapshotted from the envelope, raising the investigation TLP to the strictest
            item label

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[InvestigationItem | Problem]
    """

    kwargs = _get_kwargs(
        investigation_id=investigation_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    investigation_id: str,
    *,
    client: AuthenticatedClient,
    body: InvestigationItemInput,
) -> InvestigationItem | Problem | None:
    """Add an item to an investigation

     Pins the item with a snapshot (≤ 64 KB) so it survives retention. Adding an alert sets its 017 state
    to `investigating` when that state exists.

    **Scope:** `agent` or `admin`.

    Args:
        investigation_id (str):
        body (InvestigationItemInput): Item to add; adding an alert sets its 017 state to
            `investigating` when that state exists; 130: evidence items, attachments and any object
            reference, snapshotted from the envelope, raising the investigation TLP to the strictest
            item label

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        InvestigationItem | Problem
    """

    return sync_detailed(
        investigation_id=investigation_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    investigation_id: str,
    *,
    client: AuthenticatedClient,
    body: InvestigationItemInput,
) -> Response[InvestigationItem | Problem]:
    """Add an item to an investigation

     Pins the item with a snapshot (≤ 64 KB) so it survives retention. Adding an alert sets its 017 state
    to `investigating` when that state exists.

    **Scope:** `agent` or `admin`.

    Args:
        investigation_id (str):
        body (InvestigationItemInput): Item to add; adding an alert sets its 017 state to
            `investigating` when that state exists; 130: evidence items, attachments and any object
            reference, snapshotted from the envelope, raising the investigation TLP to the strictest
            item label

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[InvestigationItem | Problem]
    """

    kwargs = _get_kwargs(
        investigation_id=investigation_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    investigation_id: str,
    *,
    client: AuthenticatedClient,
    body: InvestigationItemInput,
) -> InvestigationItem | Problem | None:
    """Add an item to an investigation

     Pins the item with a snapshot (≤ 64 KB) so it survives retention. Adding an alert sets its 017 state
    to `investigating` when that state exists.

    **Scope:** `agent` or `admin`.

    Args:
        investigation_id (str):
        body (InvestigationItemInput): Item to add; adding an alert sets its 017 state to
            `investigating` when that state exists; 130: evidence items, attachments and any object
            reference, snapshotted from the envelope, raising the investigation TLP to the strictest
            item label

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        InvestigationItem | Problem
    """

    return (
        await asyncio_detailed(
            investigation_id=investigation_id,
            client=client,
            body=body,
        )
    ).parsed
