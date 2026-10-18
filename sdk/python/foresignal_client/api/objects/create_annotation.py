from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.annotation import Annotation
from ...models.annotation_input import AnnotationInput
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    ref: str,
    *,
    body: AnnotationInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/objects/{ref}/annotations".format(
            ref=quote(str(ref), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Annotation | Problem | None:
    if response.status_code == 201:
        response_201 = Annotation.from_dict(response.json())

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

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Annotation | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    ref: str,
    *,
    client: AuthenticatedClient,
    body: AnnotationInput,
) -> Response[Annotation | Problem]:
    """Annotate an object

     Read scope may add notes (with free tags); verdicts need agent scope (403). Verdicts feed precision
    metrics and propose historical benchmark cases; a `rejected` verdict dismisses a finding or intel
    event only with `applyStatus: true` from agent scope.

    **Scope:** `read` (token required).

    Args:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record
            refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded.
            Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from
            another instance. Example: situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (AnnotationInput): A new annotation; verdicts need agent scope. There is no tag kind:
            label objects with 130 tags; a note may carry free tags.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Annotation | Problem]
    """

    kwargs = _get_kwargs(
        ref=ref,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    ref: str,
    *,
    client: AuthenticatedClient,
    body: AnnotationInput,
) -> Annotation | Problem | None:
    """Annotate an object

     Read scope may add notes (with free tags); verdicts need agent scope (403). Verdicts feed precision
    metrics and propose historical benchmark cases; a `rejected` verdict dismisses a finding or intel
    event only with `applyStatus: true` from agent scope.

    **Scope:** `read` (token required).

    Args:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record
            refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded.
            Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from
            another instance. Example: situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (AnnotationInput): A new annotation; verdicts need agent scope. There is no tag kind:
            label objects with 130 tags; a note may carry free tags.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Annotation | Problem
    """

    return sync_detailed(
        ref=ref,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    ref: str,
    *,
    client: AuthenticatedClient,
    body: AnnotationInput,
) -> Response[Annotation | Problem]:
    """Annotate an object

     Read scope may add notes (with free tags); verdicts need agent scope (403). Verdicts feed precision
    metrics and propose historical benchmark cases; a `rejected` verdict dismisses a finding or intel
    event only with `applyStatus: true` from agent scope.

    **Scope:** `read` (token required).

    Args:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record
            refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded.
            Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from
            another instance. Example: situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (AnnotationInput): A new annotation; verdicts need agent scope. There is no tag kind:
            label objects with 130 tags; a note may carry free tags.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Annotation | Problem]
    """

    kwargs = _get_kwargs(
        ref=ref,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    ref: str,
    *,
    client: AuthenticatedClient,
    body: AnnotationInput,
) -> Annotation | Problem | None:
    """Annotate an object

     Read scope may add notes (with free tags); verdicts need agent scope (403). Verdicts feed precision
    metrics and propose historical benchmark cases; a `rejected` verdict dismisses a finding or intel
    event only with `applyStatus: true` from agent scope.

    **Scope:** `read` (token required).

    Args:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record
            refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded.
            Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from
            another instance. Example: situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (AnnotationInput): A new annotation; verdicts need agent scope. There is no tag kind:
            label objects with 130 tags; a note may carry free tags.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Annotation | Problem
    """

    return (
        await asyncio_detailed(
            ref=ref,
            client=client,
            body=body,
        )
    ).parsed
