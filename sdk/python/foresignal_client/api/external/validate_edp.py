from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.edp_validation_report import EdpValidationReport
from ...models.edp_validation_request import EdpValidationRequest
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: EdpValidationRequest,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/external/validate",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> EdpValidationReport | Problem | None:
    if response.status_code == 200:
        response_200 = EdpValidationReport.from_dict(response.json())

        return response_200

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
) -> Response[EdpValidationReport | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: EdpValidationRequest,
) -> Response[EdpValidationReport | Problem]:
    """Validate a manifest or items

     Checks a manifest, items for an existing source, or both, without storing anything. For provider
    developers and CI.

    **Scope:** `agent`.

    Args:
        body (EdpValidationRequest): Dry-run validation input

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[EdpValidationReport | Problem]
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
    body: EdpValidationRequest,
) -> EdpValidationReport | Problem | None:
    """Validate a manifest or items

     Checks a manifest, items for an existing source, or both, without storing anything. For provider
    developers and CI.

    **Scope:** `agent`.

    Args:
        body (EdpValidationRequest): Dry-run validation input

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        EdpValidationReport | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: EdpValidationRequest,
) -> Response[EdpValidationReport | Problem]:
    """Validate a manifest or items

     Checks a manifest, items for an existing source, or both, without storing anything. For provider
    developers and CI.

    **Scope:** `agent`.

    Args:
        body (EdpValidationRequest): Dry-run validation input

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[EdpValidationReport | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: EdpValidationRequest,
) -> EdpValidationReport | Problem | None:
    """Validate a manifest or items

     Checks a manifest, items for an existing source, or both, without storing anything. For provider
    developers and CI.

    **Scope:** `agent`.

    Args:
        body (EdpValidationRequest): Dry-run validation input

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        EdpValidationReport | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
