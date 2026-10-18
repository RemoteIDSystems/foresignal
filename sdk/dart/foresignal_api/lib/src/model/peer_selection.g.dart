// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'peer_selection.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PeerSelectionCWProxy {
  PeerSelection sources(List<String>? sources);

  PeerSelection kinds(List<String>? kinds);

  PeerSelection bbox(List<num>? bbox);

  PeerSelection placeId(String? placeId);

  PeerSelection maxAge(String? maxAge);

  PeerSelection backfill(String? backfill);

  PeerSelection live(bool? live);

  PeerSelection maxHops(int? maxHops);

  PeerSelection trackTtl(String? trackTtl);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PeerSelection(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PeerSelection(...).copyWith(id: 12, name: "My name")
  /// ```
  PeerSelection call({
    List<String>? sources,
    List<String>? kinds,
    List<num>? bbox,
    String? placeId,
    String? maxAge,
    String? backfill,
    bool? live,
    int? maxHops,
    String? trackTtl,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPeerSelection.copyWith(...)` or call `instanceOfPeerSelection.copyWith.fieldName(value)` for a single field.
class _$PeerSelectionCWProxyImpl implements _$PeerSelectionCWProxy {
  const _$PeerSelectionCWProxyImpl(this._value);

  final PeerSelection _value;

  @override
  PeerSelection sources(List<String>? sources) => call(sources: sources);

  @override
  PeerSelection kinds(List<String>? kinds) => call(kinds: kinds);

  @override
  PeerSelection bbox(List<num>? bbox) => call(bbox: bbox);

  @override
  PeerSelection placeId(String? placeId) => call(placeId: placeId);

  @override
  PeerSelection maxAge(String? maxAge) => call(maxAge: maxAge);

  @override
  PeerSelection backfill(String? backfill) => call(backfill: backfill);

  @override
  PeerSelection live(bool? live) => call(live: live);

  @override
  PeerSelection maxHops(int? maxHops) => call(maxHops: maxHops);

  @override
  PeerSelection trackTtl(String? trackTtl) => call(trackTtl: trackTtl);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PeerSelection(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PeerSelection(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PeerSelection call({
    Object? sources = const $CopyWithPlaceholder(),
    Object? kinds = const $CopyWithPlaceholder(),
    Object? bbox = const $CopyWithPlaceholder(),
    Object? placeId = const $CopyWithPlaceholder(),
    Object? maxAge = const $CopyWithPlaceholder(),
    Object? backfill = const $CopyWithPlaceholder(),
    Object? live = const $CopyWithPlaceholder(),
    Object? maxHops = const $CopyWithPlaceholder(),
    Object? trackTtl = const $CopyWithPlaceholder(),
  }) {
    return PeerSelection(
      sources: sources == const $CopyWithPlaceholder()
          ? _value.sources
          // ignore: cast_nullable_to_non_nullable
          : sources as List<String>?,
      kinds: kinds == const $CopyWithPlaceholder()
          ? _value.kinds
          // ignore: cast_nullable_to_non_nullable
          : kinds as List<String>?,
      bbox: bbox == const $CopyWithPlaceholder()
          ? _value.bbox
          // ignore: cast_nullable_to_non_nullable
          : bbox as List<num>?,
      placeId: placeId == const $CopyWithPlaceholder()
          ? _value.placeId
          // ignore: cast_nullable_to_non_nullable
          : placeId as String?,
      maxAge: maxAge == const $CopyWithPlaceholder()
          ? _value.maxAge
          // ignore: cast_nullable_to_non_nullable
          : maxAge as String?,
      backfill: backfill == const $CopyWithPlaceholder()
          ? _value.backfill
          // ignore: cast_nullable_to_non_nullable
          : backfill as String?,
      live: live == const $CopyWithPlaceholder()
          ? _value.live
          // ignore: cast_nullable_to_non_nullable
          : live as bool?,
      maxHops: maxHops == const $CopyWithPlaceholder()
          ? _value.maxHops
          // ignore: cast_nullable_to_non_nullable
          : maxHops as int?,
      trackTtl: trackTtl == const $CopyWithPlaceholder()
          ? _value.trackTtl
          // ignore: cast_nullable_to_non_nullable
          : trackTtl as String?,
    );
  }
}

extension $PeerSelectionCopyWith on PeerSelection {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPeerSelection.copyWith(...)` or `instanceOfPeerSelection.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PeerSelectionCWProxy get copyWith => _$PeerSelectionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PeerSelection _$PeerSelectionFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PeerSelection', json, ($checkedConvert) {
      final val = PeerSelection(
        sources: $checkedConvert(
          'sources',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        kinds: $checkedConvert(
          'kinds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        bbox: $checkedConvert(
          'bbox',
          (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
        ),
        placeId: $checkedConvert('placeId', (v) => v as String?),
        maxAge: $checkedConvert('maxAge', (v) => v as String?),
        backfill: $checkedConvert('backfill', (v) => v as String? ?? 'none'),
        live: $checkedConvert('live', (v) => v as bool? ?? false),
        maxHops: $checkedConvert('maxHops', (v) => (v as num?)?.toInt() ?? 3),
        trackTtl: $checkedConvert('trackTtl', (v) => v as String? ?? '10m'),
      );
      return val;
    });

Map<String, dynamic> _$PeerSelectionToJson(PeerSelection instance) =>
    <String, dynamic>{
      'sources': ?instance.sources,
      'kinds': ?instance.kinds,
      'bbox': ?instance.bbox,
      'placeId': ?instance.placeId,
      'maxAge': ?instance.maxAge,
      'backfill': ?instance.backfill,
      'live': ?instance.live,
      'maxHops': ?instance.maxHops,
      'trackTtl': ?instance.trackTtl,
    };
