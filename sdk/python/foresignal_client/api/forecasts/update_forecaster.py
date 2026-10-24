from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.forecaster import Forecaster
from ...models.forecaster_update import ForecasterUpdate
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    forecaster_id: str,
    *,
    body: ForecasterUpdate,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/forecasts/forecasters/{forecaster_id}".format(
            forecaster_id=quote(str(forecaster_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Forecaster | Problem | None:
    if response.status_code == 200:
        response_200 = Forecaster.from_dict(response.json())

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
) -> Response[Forecaster | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    forecaster_id: str,
    *,
    client: AuthenticatedClient,
    body: ForecasterUpdate,
) -> Response[Forecaster | Problem]:
    """Update a forecaster

     Sets a forecaster's `active` flag, configuration and, for an external forecaster, display `name`
    (155).

    **Scope:** `admin`.

    Args:
        forecaster_id (str):
        body (ForecasterUpdate): Replace a forecaster's settings

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Forecaster | Problem]
    """

    kwargs = _get_kwargs(
        forecaster_id=forecaster_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    forecaster_id: str,
    *,
    client: AuthenticatedClient,
    body: ForecasterUpdate,
) -> Forecaster | Problem | None:
    """Update a forecaster

     Sets a forecaster's `active` flag, configuration and, for an external forecaster, display `name`
    (155).

    **Scope:** `admin`.

    Args:
        forecaster_id (str):
        body (ForecasterUpdate): Replace a forecaster's settings

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Forecaster | Problem
    """

    return sync_detailed(
        forecaster_id=forecaster_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    forecaster_id: str,
    *,
    client: AuthenticatedClient,
    body: ForecasterUpdate,
) -> Response[Forecaster | Problem]:
    """Update a forecaster

     Sets a forecaster's `active` flag, configuration and, for an external forecaster, display `name`
    (155).

    **Scope:** `admin`.

    Args:
        forecaster_id (str):
        body (ForecasterUpdate): Replace a forecaster's settings

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Forecaster | Problem]
    """

    kwargs = _get_kwargs(
        forecaster_id=forecaster_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    forecaster_id: str,
    *,
    client: AuthenticatedClient,
    body: ForecasterUpdate,
) -> Forecaster | Problem | None:
    """Update a forecaster

     Sets a forecaster's `active` flag, configuration and, for an external forecaster, display `name`
    (155).

    **Scope:** `admin`.

    Args:
        forecaster_id (str):
        body (ForecasterUpdate): Replace a forecaster's settings

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Forecaster | Problem
    """

    return (
        await asyncio_detailed(
            forecaster_id=forecaster_id,
            client=client,
            body=body,
        )
    ).parsed
