from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.custom_source import CustomSource
from ...models.custom_source_input import CustomSourceInput
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    source_id: str,
    *,
    body: CustomSourceInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/custom-sources/{source_id}".format(
            source_id=quote(str(source_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> CustomSource | Problem | None:
    if response.status_code == 200:
        response_200 = CustomSource.from_dict(response.json())

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

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[CustomSource | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    source_id: str,
    *,
    client: AuthenticatedClient,
    body: CustomSourceInput,
) -> Response[CustomSource | Problem]:
    """Update a custom source

     The id cannot change. Removing a kind that has current heads → 409 KIND_IN_USE; changing
    `observedAtMode` while heads exist → 409 OBSERVED_AT_MODE_LOCKED; a SAMPLED/LATEST policy with
    emptied `significantFields` → 400 INGEST_POLICY_INVALID. Changed
    `volatileFields`/`significantFields` trigger the writer's lazy recipe migration. Verifier secrets
    are write-only (omit to keep).

    **Scope:** `admin`.

    Args:
        source_id (str):
        body (CustomSourceInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[CustomSource | Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    source_id: str,
    *,
    client: AuthenticatedClient,
    body: CustomSourceInput,
) -> CustomSource | Problem | None:
    """Update a custom source

     The id cannot change. Removing a kind that has current heads → 409 KIND_IN_USE; changing
    `observedAtMode` while heads exist → 409 OBSERVED_AT_MODE_LOCKED; a SAMPLED/LATEST policy with
    emptied `significantFields` → 400 INGEST_POLICY_INVALID. Changed
    `volatileFields`/`significantFields` trigger the writer's lazy recipe migration. Verifier secrets
    are write-only (omit to keep).

    **Scope:** `admin`.

    Args:
        source_id (str):
        body (CustomSourceInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        CustomSource | Problem
    """

    return sync_detailed(
        source_id=source_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    source_id: str,
    *,
    client: AuthenticatedClient,
    body: CustomSourceInput,
) -> Response[CustomSource | Problem]:
    """Update a custom source

     The id cannot change. Removing a kind that has current heads → 409 KIND_IN_USE; changing
    `observedAtMode` while heads exist → 409 OBSERVED_AT_MODE_LOCKED; a SAMPLED/LATEST policy with
    emptied `significantFields` → 400 INGEST_POLICY_INVALID. Changed
    `volatileFields`/`significantFields` trigger the writer's lazy recipe migration. Verifier secrets
    are write-only (omit to keep).

    **Scope:** `admin`.

    Args:
        source_id (str):
        body (CustomSourceInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[CustomSource | Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    source_id: str,
    *,
    client: AuthenticatedClient,
    body: CustomSourceInput,
) -> CustomSource | Problem | None:
    """Update a custom source

     The id cannot change. Removing a kind that has current heads → 409 KIND_IN_USE; changing
    `observedAtMode` while heads exist → 409 OBSERVED_AT_MODE_LOCKED; a SAMPLED/LATEST policy with
    emptied `significantFields` → 400 INGEST_POLICY_INVALID. Changed
    `volatileFields`/`significantFields` trigger the writer's lazy recipe migration. Verifier secrets
    are write-only (omit to keep).

    **Scope:** `admin`.

    Args:
        source_id (str):
        body (CustomSourceInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        CustomSource | Problem
    """

    return (
        await asyncio_detailed(
            source_id=source_id,
            client=client,
            body=body,
        )
    ).parsed
