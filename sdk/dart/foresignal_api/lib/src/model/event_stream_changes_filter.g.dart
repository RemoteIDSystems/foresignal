// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'event_stream_changes_filter.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EventStreamChangesFilterCWProxy {
  EventStreamChangesFilter source_(List<String>? source_);

  EventStreamChangesFilter kind(List<String>? kind);

  EventStreamChangesFilter externalId(List<String>? externalId);

  EventStreamChangesFilter bbox(String? bbox);

  EventStreamChangesFilter near(String? near);

  EventStreamChangesFilter radiusKm(num? radiusKm);

  EventStreamChangesFilter live(bool? live);

  EventStreamChangesFilter touches(bool? touches);

  EventStreamChangesFilter payload(bool? payload);

  EventStreamChangesFilter place(List<String>? place);

  EventStreamChangesFilter view(String? view);

  EventStreamChangesFilter inWatchlist(String? inWatchlist);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventStreamChangesFilter(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventStreamChangesFilter(...).copyWith(id: 12, name: "My name")
  /// ```
  EventStreamChangesFilter call({
    List<String>? source_,
    List<String>? kind,
    List<String>? externalId,
    String? bbox,
    String? near,
    num? radiusKm,
    bool? live,
    bool? touches,
    bool? payload,
    List<String>? place,
    String? view,
    String? inWatchlist,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEventStreamChangesFilter.copyWith(...)` or call `instanceOfEventStreamChangesFilter.copyWith.fieldName(value)` for a single field.
class _$EventStreamChangesFilterCWProxyImpl
    implements _$EventStreamChangesFilterCWProxy {
  const _$EventStreamChangesFilterCWProxyImpl(this._value);

  final EventStreamChangesFilter _value;

  @override
  EventStreamChangesFilter source_(List<String>? source_) =>
      call(source_: source_);

  @override
  EventStreamChangesFilter kind(List<String>? kind) => call(kind: kind);

  @override
  EventStreamChangesFilter externalId(List<String>? externalId) =>
      call(externalId: externalId);

  @override
  EventStreamChangesFilter bbox(String? bbox) => call(bbox: bbox);

  @override
  EventStreamChangesFilter near(String? near) => call(near: near);

  @override
  EventStreamChangesFilter radiusKm(num? radiusKm) => call(radiusKm: radiusKm);

  @override
  EventStreamChangesFilter live(bool? live) => call(live: live);

  @override
  EventStreamChangesFilter touches(bool? touches) => call(touches: touches);

  @override
  EventStreamChangesFilter payload(bool? payload) => call(payload: payload);

  @override
  EventStreamChangesFilter place(List<String>? place) => call(place: place);

  @override
  EventStreamChangesFilter view(String? view) => call(view: view);

  @override
  EventStreamChangesFilter inWatchlist(String? inWatchlist) =>
      call(inWatchlist: inWatchlist);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventStreamChangesFilter(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventStreamChangesFilter(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EventStreamChangesFilter call({
    Object? source_ = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
    Object? bbox = const $CopyWithPlaceholder(),
    Object? near = const $CopyWithPlaceholder(),
    Object? radiusKm = const $CopyWithPlaceholder(),
    Object? live = const $CopyWithPlaceholder(),
    Object? touches = const $CopyWithPlaceholder(),
    Object? payload = const $CopyWithPlaceholder(),
    Object? place = const $CopyWithPlaceholder(),
    Object? view = const $CopyWithPlaceholder(),
    Object? inWatchlist = const $CopyWithPlaceholder(),
  }) {
    return EventStreamChangesFilter(
      source_: source_ == const $CopyWithPlaceholder()
          ? _value.source_
          // ignore: cast_nullable_to_non_nullable
          : source_ as List<String>?,
      kind: kind == const $CopyWithPlaceholder()
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as List<String>?,
      externalId: externalId == const $CopyWithPlaceholder()
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as List<String>?,
      bbox: bbox == const $CopyWithPlaceholder()
          ? _value.bbox
          // ignore: cast_nullable_to_non_nullable
          : bbox as String?,
      near: near == const $CopyWithPlaceholder()
          ? _value.near
          // ignore: cast_nullable_to_non_nullable
          : near as String?,
      radiusKm: radiusKm == const $CopyWithPlaceholder()
          ? _value.radiusKm
          // ignore: cast_nullable_to_non_nullable
          : radiusKm as num?,
      live: live == const $CopyWithPlaceholder()
          ? _value.live
          // ignore: cast_nullable_to_non_nullable
          : live as bool?,
      touches: touches == const $CopyWithPlaceholder()
          ? _value.touches
          // ignore: cast_nullable_to_non_nullable
          : touches as bool?,
      payload: payload == const $CopyWithPlaceholder()
          ? _value.payload
          // ignore: cast_nullable_to_non_nullable
          : payload as bool?,
      place: place == const $CopyWithPlaceholder()
          ? _value.place
          // ignore: cast_nullable_to_non_nullable
          : place as List<String>?,
      view: view == const $CopyWithPlaceholder()
          ? _value.view
          // ignore: cast_nullable_to_non_nullable
          : view as String?,
      inWatchlist: inWatchlist == const $CopyWithPlaceholder()
          ? _value.inWatchlist
          // ignore: cast_nullable_to_non_nullable
          : inWatchlist as String?,
    );
  }
}

extension $EventStreamChangesFilterCopyWith on EventStreamChangesFilter {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEventStreamChangesFilter.copyWith(...)` or `instanceOfEventStreamChangesFilter.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EventStreamChangesFilterCWProxy get copyWith =>
      _$EventStreamChangesFilterCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EventStreamChangesFilter _$EventStreamChangesFilterFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EventStreamChangesFilter', json, ($checkedConvert) {
  final val = EventStreamChangesFilter(
    source_: $checkedConvert(
      'source',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    kind: $checkedConvert(
      'kind',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    externalId: $checkedConvert(
      'externalId',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    bbox: $checkedConvert('bbox', (v) => v as String?),
    near: $checkedConvert('near', (v) => v as String?),
    radiusKm: $checkedConvert('radiusKm', (v) => v as num?),
    live: $checkedConvert('live', (v) => v as bool? ?? true),
    touches: $checkedConvert('touches', (v) => v as bool? ?? false),
    payload: $checkedConvert('payload', (v) => v as bool? ?? false),
    place: $checkedConvert(
      'place',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    view: $checkedConvert('view', (v) => v as String?),
    inWatchlist: $checkedConvert('inWatchlist', (v) => v as String?),
  );
  return val;
}, fieldKeyMap: const {'source_': 'source'});

Map<String, dynamic> _$EventStreamChangesFilterToJson(
  EventStreamChangesFilter instance,
) => <String, dynamic>{
  'source': ?instance.source_,
  'kind': ?instance.kind,
  'externalId': ?instance.externalId,
  'bbox': ?instance.bbox,
  'near': ?instance.near,
  'radiusKm': ?instance.radiusKm,
  'live': ?instance.live,
  'touches': ?instance.touches,
  'payload': ?instance.payload,
  'place': ?instance.place,
  'view': ?instance.view,
  'inWatchlist': ?instance.inWatchlist,
};
