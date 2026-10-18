import datetime
from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_product_timeline_mode import GetProductTimelineMode
from ...models.problem import Problem
from ...models.product_timeline import ProductTimeline
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    products: list[str] | Unset = UNSET,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    mode: GetProductTimelineMode | Unset = UNSET,
    past: str | Unset = UNSET,
    future: str | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_products: list[str] | Unset = UNSET
    if not isinstance(products, Unset):
        json_products = products

    params["products"] = json_products

    json_from_: str | Unset = UNSET
    if not isinstance(from_, Unset):
        json_from_ = from_.isoformat()
    params["from"] = json_from_

    json_to: str | Unset = UNSET
    if not isinstance(to, Unset):
        json_to = to.isoformat()
    params["to"] = json_to

    json_mode: str | Unset = UNSET
    if not isinstance(mode, Unset):
        json_mode = mode.value

    params["mode"] = json_mode

    params["past"] = past

    params["future"] = future

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/products/timeline",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | ProductTimeline | None:
    if response.status_code == 200:
        response_200 = ProductTimeline.from_dict(response.json())

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

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if response.status_code == 503:
        response_503 = Problem.from_dict(response.json())

        return response_503

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | ProductTimeline]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    products: list[str] | Unset = UNSET,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    mode: GetProductTimelineMode | Unset = UNSET,
    past: str | Unset = UNSET,
    future: str | Unset = UNSET,
) -> Response[Problem | ProductTimeline]:
    """Get the cross-product timeline

     Ticks marked `observed|forecast`: the union of the products' frame valid times in the window, with
    the frame chosen per product per tick (at or before the tick with the smallest gap, honoring
    `maxGap`; a product with none is absent at that tick), the single run used per forecast product (its
    newest complete run, never mixed; `runs`), `nowIndex` (the tick nearest now) and expanded, content-
    addressed tile templates. `mode=seamless` joins a `past` observed product (ticks up to now) to a
    `future` forecast product (ticks after now). External catalog entries get synthesized ticks every
    `timeDimension.period` up to `now − defaultLag`, with `availability: unverified` and the external
    template's `{time}` filled. Default window: now − 6 h to now (+ 48 h when a forecast product is
    named); seamless now − 3 h to now + 18 h. At most 10 products. More than 500 ticks → 400
    VALIDATION_FAILED.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        products (list[str] | Unset):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        mode (GetProductTimelineMode | Unset):
        past (str | Unset):
        future (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ProductTimeline]
    """

    kwargs = _get_kwargs(
        products=products,
        from_=from_,
        to=to,
        mode=mode,
        past=past,
        future=future,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    products: list[str] | Unset = UNSET,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    mode: GetProductTimelineMode | Unset = UNSET,
    past: str | Unset = UNSET,
    future: str | Unset = UNSET,
) -> Problem | ProductTimeline | None:
    """Get the cross-product timeline

     Ticks marked `observed|forecast`: the union of the products' frame valid times in the window, with
    the frame chosen per product per tick (at or before the tick with the smallest gap, honoring
    `maxGap`; a product with none is absent at that tick), the single run used per forecast product (its
    newest complete run, never mixed; `runs`), `nowIndex` (the tick nearest now) and expanded, content-
    addressed tile templates. `mode=seamless` joins a `past` observed product (ticks up to now) to a
    `future` forecast product (ticks after now). External catalog entries get synthesized ticks every
    `timeDimension.period` up to `now − defaultLag`, with `availability: unverified` and the external
    template's `{time}` filled. Default window: now − 6 h to now (+ 48 h when a forecast product is
    named); seamless now − 3 h to now + 18 h. At most 10 products. More than 500 ticks → 400
    VALIDATION_FAILED.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        products (list[str] | Unset):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        mode (GetProductTimelineMode | Unset):
        past (str | Unset):
        future (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ProductTimeline
    """

    return sync_detailed(
        client=client,
        products=products,
        from_=from_,
        to=to,
        mode=mode,
        past=past,
        future=future,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    products: list[str] | Unset = UNSET,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    mode: GetProductTimelineMode | Unset = UNSET,
    past: str | Unset = UNSET,
    future: str | Unset = UNSET,
) -> Response[Problem | ProductTimeline]:
    """Get the cross-product timeline

     Ticks marked `observed|forecast`: the union of the products' frame valid times in the window, with
    the frame chosen per product per tick (at or before the tick with the smallest gap, honoring
    `maxGap`; a product with none is absent at that tick), the single run used per forecast product (its
    newest complete run, never mixed; `runs`), `nowIndex` (the tick nearest now) and expanded, content-
    addressed tile templates. `mode=seamless` joins a `past` observed product (ticks up to now) to a
    `future` forecast product (ticks after now). External catalog entries get synthesized ticks every
    `timeDimension.period` up to `now − defaultLag`, with `availability: unverified` and the external
    template's `{time}` filled. Default window: now − 6 h to now (+ 48 h when a forecast product is
    named); seamless now − 3 h to now + 18 h. At most 10 products. More than 500 ticks → 400
    VALIDATION_FAILED.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        products (list[str] | Unset):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        mode (GetProductTimelineMode | Unset):
        past (str | Unset):
        future (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ProductTimeline]
    """

    kwargs = _get_kwargs(
        products=products,
        from_=from_,
        to=to,
        mode=mode,
        past=past,
        future=future,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    products: list[str] | Unset = UNSET,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    mode: GetProductTimelineMode | Unset = UNSET,
    past: str | Unset = UNSET,
    future: str | Unset = UNSET,
) -> Problem | ProductTimeline | None:
    """Get the cross-product timeline

     Ticks marked `observed|forecast`: the union of the products' frame valid times in the window, with
    the frame chosen per product per tick (at or before the tick with the smallest gap, honoring
    `maxGap`; a product with none is absent at that tick), the single run used per forecast product (its
    newest complete run, never mixed; `runs`), `nowIndex` (the tick nearest now) and expanded, content-
    addressed tile templates. `mode=seamless` joins a `past` observed product (ticks up to now) to a
    `future` forecast product (ticks after now). External catalog entries get synthesized ticks every
    `timeDimension.period` up to `now − defaultLag`, with `availability: unverified` and the external
    template's `{time}` filled. Default window: now − 6 h to now (+ 48 h when a forecast product is
    named); seamless now − 3 h to now + 18 h. At most 10 products. More than 500 ticks → 400
    VALIDATION_FAILED.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        products (list[str] | Unset):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        mode (GetProductTimelineMode | Unset):
        past (str | Unset):
        future (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ProductTimeline
    """

    return (
        await asyncio_detailed(
            client=client,
            products=products,
            from_=from_,
            to=to,
            mode=mode,
            past=past,
            future=future,
        )
    ).parsed
