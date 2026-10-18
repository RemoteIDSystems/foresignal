from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.edp_batch import EdpBatch
from ...models.edp_ingest_result import EdpIngestResult
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    source_id: str,
    *,
    body: EdpBatch,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/external/sources/{source_id}/items".format(
            source_id=quote(str(source_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> EdpIngestResult | Problem | None:
    if response.status_code == 200:
        response_200 = EdpIngestResult.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if response.status_code == 413:
        response_413 = Problem.from_dict(response.json())

        return response_413

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[EdpIngestResult | Problem]:
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
    body: EdpBatch,
) -> Response[EdpIngestResult | Problem]:
    """Push EDP items

     Accepts a batch (JSON) or NDJSON items; validates each item against its stream and returns per-item
    rejections. Pending or suspended sources answer 409.

    **Scope:** ingest key of the source (or `admin`).

    Args:
        source_id (str):
        body (EdpBatch): A batch of EDP items (push body or pull response)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[EdpIngestResult | Problem]
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
    body: EdpBatch,
) -> EdpIngestResult | Problem | None:
    """Push EDP items

     Accepts a batch (JSON) or NDJSON items; validates each item against its stream and returns per-item
    rejections. Pending or suspended sources answer 409.

    **Scope:** ingest key of the source (or `admin`).

    Args:
        source_id (str):
        body (EdpBatch): A batch of EDP items (push body or pull response)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        EdpIngestResult | Problem
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
    body: EdpBatch,
) -> Response[EdpIngestResult | Problem]:
    """Push EDP items

     Accepts a batch (JSON) or NDJSON items; validates each item against its stream and returns per-item
    rejections. Pending or suspended sources answer 409.

    **Scope:** ingest key of the source (or `admin`).

    Args:
        source_id (str):
        body (EdpBatch): A batch of EDP items (push body or pull response)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[EdpIngestResult | Problem]
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
    body: EdpBatch,
) -> EdpIngestResult | Problem | None:
    """Push EDP items

     Accepts a batch (JSON) or NDJSON items; validates each item against its stream and returns per-item
    rejections. Pending or suspended sources answer 409.

    **Scope:** ingest key of the source (or `admin`).

    Args:
        source_id (str):
        body (EdpBatch): A batch of EDP items (push body or pull response)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        EdpIngestResult | Problem
    """

    return (
        await asyncio_detailed(
            source_id=source_id,
            client=client,
            body=body,
        )
    ).parsed
