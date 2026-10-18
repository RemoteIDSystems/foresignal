from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.reference_import import ReferenceImport
from ...models.reference_import_request import ReferenceImportRequest
from ...types import Response


def _get_kwargs(
    *,
    body: ReferenceImportRequest,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/reference-imports",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | ReferenceImport | None:
    if response.status_code == 202:
        response_202 = ReferenceImport.from_dict(response.json())

        return response_202

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | ReferenceImport]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: ReferenceImportRequest,
) -> Response[Problem | ReferenceImport]:
    """Start a reference-history import

     Starts a resumable, idempotent import (per dataset and period) of weekly counts from UCDP GED,
    ACLED, USGS ComCat or GDACS. Rows keep their licence and egress (`LOCAL_ONLY` for ACLED); 409
    `IMPORT_DATASET_UNAVAILABLE` when the file, API or credentials are not available.

    **Scope:** `admin`.

    Args:
        body (ReferenceImportRequest): Start a reference-history import (counts only, licence
            kept)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ReferenceImport]
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
    body: ReferenceImportRequest,
) -> Problem | ReferenceImport | None:
    """Start a reference-history import

     Starts a resumable, idempotent import (per dataset and period) of weekly counts from UCDP GED,
    ACLED, USGS ComCat or GDACS. Rows keep their licence and egress (`LOCAL_ONLY` for ACLED); 409
    `IMPORT_DATASET_UNAVAILABLE` when the file, API or credentials are not available.

    **Scope:** `admin`.

    Args:
        body (ReferenceImportRequest): Start a reference-history import (counts only, licence
            kept)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ReferenceImport
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: ReferenceImportRequest,
) -> Response[Problem | ReferenceImport]:
    """Start a reference-history import

     Starts a resumable, idempotent import (per dataset and period) of weekly counts from UCDP GED,
    ACLED, USGS ComCat or GDACS. Rows keep their licence and egress (`LOCAL_ONLY` for ACLED); 409
    `IMPORT_DATASET_UNAVAILABLE` when the file, API or credentials are not available.

    **Scope:** `admin`.

    Args:
        body (ReferenceImportRequest): Start a reference-history import (counts only, licence
            kept)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ReferenceImport]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: ReferenceImportRequest,
) -> Problem | ReferenceImport | None:
    """Start a reference-history import

     Starts a resumable, idempotent import (per dataset and period) of weekly counts from UCDP GED,
    ACLED, USGS ComCat or GDACS. Rows keep their licence and egress (`LOCAL_ONLY` for ACLED); 409
    `IMPORT_DATASET_UNAVAILABLE` when the file, API or credentials are not available.

    **Scope:** `admin`.

    Args:
        body (ReferenceImportRequest): Start a reference-history import (counts only, licence
            kept)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ReferenceImport
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
