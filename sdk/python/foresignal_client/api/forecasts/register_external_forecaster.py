from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.external_forecaster_registration import ExternalForecasterRegistration
from ...models.forecaster import Forecaster
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: ExternalForecasterRegistration,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/forecasts/forecasters",
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
    *,
    client: AuthenticatedClient,
    body: ExternalForecasterRegistration,
) -> Response[Forecaster | Problem]:
    """Register an external forecaster

     Pre-registers, names and configures a first-class external forecaster (155) — a display name and a
    per-forecaster `externalMinResolved` — independent of the auto-derived `edp:{sourceId}:{streamId}`
    id, before any forecast arrives. An existing forecaster with that id is renamed and reconfigured in
    place (its type is kept).

    **Scope:** `admin`.

    Args:
        body (ExternalForecasterRegistration): Pre-registers, names and configures a first-class
            external forecaster (155), independent of the auto-derived edp:{sourceId}:{streamId} id.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Forecaster | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    body: ExternalForecasterRegistration,
) -> Forecaster | Problem | None:
    """Register an external forecaster

     Pre-registers, names and configures a first-class external forecaster (155) — a display name and a
    per-forecaster `externalMinResolved` — independent of the auto-derived `edp:{sourceId}:{streamId}`
    id, before any forecast arrives. An existing forecaster with that id is renamed and reconfigured in
    place (its type is kept).

    **Scope:** `admin`.

    Args:
        body (ExternalForecasterRegistration): Pre-registers, names and configures a first-class
            external forecaster (155), independent of the auto-derived edp:{sourceId}:{streamId} id.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Forecaster | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: ExternalForecasterRegistration,
) -> Response[Forecaster | Problem]:
    """Register an external forecaster

     Pre-registers, names and configures a first-class external forecaster (155) — a display name and a
    per-forecaster `externalMinResolved` — independent of the auto-derived `edp:{sourceId}:{streamId}`
    id, before any forecast arrives. An existing forecaster with that id is renamed and reconfigured in
    place (its type is kept).

    **Scope:** `admin`.

    Args:
        body (ExternalForecasterRegistration): Pre-registers, names and configures a first-class
            external forecaster (155), independent of the auto-derived edp:{sourceId}:{streamId} id.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Forecaster | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: ExternalForecasterRegistration,
) -> Forecaster | Problem | None:
    """Register an external forecaster

     Pre-registers, names and configures a first-class external forecaster (155) — a display name and a
    per-forecaster `externalMinResolved` — independent of the auto-derived `edp:{sourceId}:{streamId}`
    id, before any forecast arrives. An existing forecaster with that id is renamed and reconfigured in
    place (its type is kept).

    **Scope:** `admin`.

    Args:
        body (ExternalForecasterRegistration): Pre-registers, names and configures a first-class
            external forecaster (155), independent of the auto-derived edp:{sourceId}:{streamId} id.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Forecaster | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
