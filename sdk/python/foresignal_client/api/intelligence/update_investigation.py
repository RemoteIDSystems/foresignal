from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.investigation import Investigation
from ...models.investigation_patch import InvestigationPatch
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    investigation_id: str,
    *,
    body: InvestigationPatch,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "patch",
        "url": "/v1/investigations/{investigation_id}".format(
            investigation_id=quote(str(investigation_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Investigation | Problem | None:
    if response.status_code == 200:
        response_200 = Investigation.from_dict(response.json())

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

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Investigation | Problem]:
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
    body: InvestigationPatch,
) -> Response[Investigation | Problem]:
    """Update an investigation

     Changes title, status, summary and tags.

    **Scope:** `agent` or `admin`.

    Args:
        investigation_id (str):
        body (InvestigationPatch): Changes to an investigation (editor); owner, visibility, TLP
            and grants change through PUT /v1/objects/{ref}/acl

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Investigation | Problem]
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
    body: InvestigationPatch,
) -> Investigation | Problem | None:
    """Update an investigation

     Changes title, status, summary and tags.

    **Scope:** `agent` or `admin`.

    Args:
        investigation_id (str):
        body (InvestigationPatch): Changes to an investigation (editor); owner, visibility, TLP
            and grants change through PUT /v1/objects/{ref}/acl

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Investigation | Problem
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
    body: InvestigationPatch,
) -> Response[Investigation | Problem]:
    """Update an investigation

     Changes title, status, summary and tags.

    **Scope:** `agent` or `admin`.

    Args:
        investigation_id (str):
        body (InvestigationPatch): Changes to an investigation (editor); owner, visibility, TLP
            and grants change through PUT /v1/objects/{ref}/acl

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Investigation | Problem]
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
    body: InvestigationPatch,
) -> Investigation | Problem | None:
    """Update an investigation

     Changes title, status, summary and tags.

    **Scope:** `agent` or `admin`.

    Args:
        investigation_id (str):
        body (InvestigationPatch): Changes to an investigation (editor); owner, visibility, TLP
            and grants change through PUT /v1/objects/{ref}/acl

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Investigation | Problem
    """

    return (
        await asyncio_detailed(
            investigation_id=investigation_id,
            client=client,
            body=body,
        )
    ).parsed
