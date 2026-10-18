from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.regional_index_def import RegionalIndexDef
from ...models.regional_index_def_input import RegionalIndexDefInput
from ...types import Response


def _get_kwargs(
    index_id: str,
    *,
    body: RegionalIndexDefInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/indices/{index_id}".format(
            index_id=quote(str(index_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | RegionalIndexDef | None:
    if response.status_code == 200:
        response_200 = RegionalIndexDef.from_dict(response.json())

        return response_200

    if response.status_code == 201:
        response_201 = RegionalIndexDef.from_dict(response.json())

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

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | RegionalIndexDef]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    index_id: str,
    *,
    client: AuthenticatedClient,
    body: RegionalIndexDefInput,
) -> Response[Problem | RegionalIndexDef]:
    """Create or replace a regional index

     Replaces a definition and its enabled flag, or creates an operator-defined index. Weights must sum
    to 1.

    **Scope:** `agent`.

    Args:
        index_id (str):  Example: instability.
        body (RegionalIndexDefInput): Definition and enablement of a regional index (PUT replaces)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | RegionalIndexDef]
    """

    kwargs = _get_kwargs(
        index_id=index_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    index_id: str,
    *,
    client: AuthenticatedClient,
    body: RegionalIndexDefInput,
) -> Problem | RegionalIndexDef | None:
    """Create or replace a regional index

     Replaces a definition and its enabled flag, or creates an operator-defined index. Weights must sum
    to 1.

    **Scope:** `agent`.

    Args:
        index_id (str):  Example: instability.
        body (RegionalIndexDefInput): Definition and enablement of a regional index (PUT replaces)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | RegionalIndexDef
    """

    return sync_detailed(
        index_id=index_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    index_id: str,
    *,
    client: AuthenticatedClient,
    body: RegionalIndexDefInput,
) -> Response[Problem | RegionalIndexDef]:
    """Create or replace a regional index

     Replaces a definition and its enabled flag, or creates an operator-defined index. Weights must sum
    to 1.

    **Scope:** `agent`.

    Args:
        index_id (str):  Example: instability.
        body (RegionalIndexDefInput): Definition and enablement of a regional index (PUT replaces)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | RegionalIndexDef]
    """

    kwargs = _get_kwargs(
        index_id=index_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    index_id: str,
    *,
    client: AuthenticatedClient,
    body: RegionalIndexDefInput,
) -> Problem | RegionalIndexDef | None:
    """Create or replace a regional index

     Replaces a definition and its enabled flag, or creates an operator-defined index. Weights must sum
    to 1.

    **Scope:** `agent`.

    Args:
        index_id (str):  Example: instability.
        body (RegionalIndexDefInput): Definition and enablement of a regional index (PUT replaces)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | RegionalIndexDef
    """

    return (
        await asyncio_detailed(
            index_id=index_id,
            client=client,
            body=body,
        )
    ).parsed
