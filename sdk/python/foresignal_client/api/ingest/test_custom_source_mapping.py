from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.mapping_test_request import MappingTestRequest
from ...models.mapping_test_result import MappingTestResult
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    source_id: str,
    *,
    body: MappingTestRequest | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/custom-sources/{source_id}/mapping/test".format(
            source_id=quote(str(source_id), safe=""),
        ),
    }

    if not isinstance(body, Unset):
        _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> MappingTestResult | Problem | None:
    if response.status_code == 200:
        response_200 = MappingTestResult.from_dict(response.json())

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
) -> Response[MappingTestResult | Problem]:
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
    body: MappingTestRequest | Unset = UNSET,
) -> Response[MappingTestResult | Problem]:
    """Run a mapping over captured samples

     Runs the proposed mapping (body) or, when the body is empty or omits `mapping`, the stored one over
    the source's captured samples. Writes nothing. 400 `MAPPING_INVALID` for a mapping that does not
    compile.

    **Scope:** `admin`.

    Args:
        source_id (str):
        body (MappingTestRequest | Unset): Optional proposed mapping to test against captured
            samples; omit to test the stored mapping (059)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[MappingTestResult | Problem]
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
    body: MappingTestRequest | Unset = UNSET,
) -> MappingTestResult | Problem | None:
    """Run a mapping over captured samples

     Runs the proposed mapping (body) or, when the body is empty or omits `mapping`, the stored one over
    the source's captured samples. Writes nothing. 400 `MAPPING_INVALID` for a mapping that does not
    compile.

    **Scope:** `admin`.

    Args:
        source_id (str):
        body (MappingTestRequest | Unset): Optional proposed mapping to test against captured
            samples; omit to test the stored mapping (059)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        MappingTestResult | Problem
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
    body: MappingTestRequest | Unset = UNSET,
) -> Response[MappingTestResult | Problem]:
    """Run a mapping over captured samples

     Runs the proposed mapping (body) or, when the body is empty or omits `mapping`, the stored one over
    the source's captured samples. Writes nothing. 400 `MAPPING_INVALID` for a mapping that does not
    compile.

    **Scope:** `admin`.

    Args:
        source_id (str):
        body (MappingTestRequest | Unset): Optional proposed mapping to test against captured
            samples; omit to test the stored mapping (059)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[MappingTestResult | Problem]
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
    body: MappingTestRequest | Unset = UNSET,
) -> MappingTestResult | Problem | None:
    """Run a mapping over captured samples

     Runs the proposed mapping (body) or, when the body is empty or omits `mapping`, the stored one over
    the source's captured samples. Writes nothing. 400 `MAPPING_INVALID` for a mapping that does not
    compile.

    **Scope:** `admin`.

    Args:
        source_id (str):
        body (MappingTestRequest | Unset): Optional proposed mapping to test against captured
            samples; omit to test the stored mapping (059)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        MappingTestResult | Problem
    """

    return (
        await asyncio_detailed(
            source_id=source_id,
            client=client,
            body=body,
        )
    ).parsed
