// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'external_stream_stats.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExternalStreamStatsCWProxy {
  ExternalStreamStats streamId(String? streamId);

  ExternalStreamStats mode(String? mode);

  ExternalStreamStats kind(String? kind);

  ExternalStreamStats items(int? items);

  ExternalStreamStats rejected(int? rejected);

  ExternalStreamStats lastItemAt(DateTime? lastItemAt);

  ExternalStreamStats bindings(int? bindings);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExternalStreamStats(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExternalStreamStats(...).copyWith(id: 12, name: "My name")
  /// ```
  ExternalStreamStats call({
    String? streamId,
    String? mode,
    String? kind,
    int? items,
    int? rejected,
    DateTime? lastItemAt,
    int? bindings,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExternalStreamStats.copyWith(...)` or call `instanceOfExternalStreamStats.copyWith.fieldName(value)` for a single field.
class _$ExternalStreamStatsCWProxyImpl implements _$ExternalStreamStatsCWProxy {
  const _$ExternalStreamStatsCWProxyImpl(this._value);

  final ExternalStreamStats _value;

  @override
  ExternalStreamStats streamId(String? streamId) => call(streamId: streamId);

  @override
  ExternalStreamStats mode(String? mode) => call(mode: mode);

  @override
  ExternalStreamStats kind(String? kind) => call(kind: kind);

  @override
  ExternalStreamStats items(int? items) => call(items: items);

  @override
  ExternalStreamStats rejected(int? rejected) => call(rejected: rejected);

  @override
  ExternalStreamStats lastItemAt(DateTime? lastItemAt) =>
      call(lastItemAt: lastItemAt);

  @override
  ExternalStreamStats bindings(int? bindings) => call(bindings: bindings);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExternalStreamStats(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExternalStreamStats(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExternalStreamStats call({
    Object? streamId = const $CopyWithPlaceholder(),
    Object? mode = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? items = const $CopyWithPlaceholder(),
    Object? rejected = const $CopyWithPlaceholder(),
    Object? lastItemAt = const $CopyWithPlaceholder(),
    Object? bindings = const $CopyWithPlaceholder(),
  }) {
    return ExternalStreamStats(
      streamId: streamId == const $CopyWithPlaceholder()
          ? _value.streamId
          // ignore: cast_nullable_to_non_nullable
          : streamId as String?,
      mode: mode == const $CopyWithPlaceholder()
          ? _value.mode
          // ignore: cast_nullable_to_non_nullable
          : mode as String?,
      kind: kind == const $CopyWithPlaceholder()
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as String?,
      items: items == const $CopyWithPlaceholder()
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as int?,
      rejected: rejected == const $CopyWithPlaceholder()
          ? _value.rejected
          // ignore: cast_nullable_to_non_nullable
          : rejected as int?,
      lastItemAt: lastItemAt == const $CopyWithPlaceholder()
          ? _value.lastItemAt
          // ignore: cast_nullable_to_non_nullable
          : lastItemAt as DateTime?,
      bindings: bindings == const $CopyWithPlaceholder()
          ? _value.bindings
          // ignore: cast_nullable_to_non_nullable
          : bindings as int?,
    );
  }
}

extension $ExternalStreamStatsCopyWith on ExternalStreamStats {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExternalStreamStats.copyWith(...)` or `instanceOfExternalStreamStats.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExternalStreamStatsCWProxy get copyWith =>
      _$ExternalStreamStatsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExternalStreamStats _$ExternalStreamStatsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ExternalStreamStats', json, ($checkedConvert) {
      final val = ExternalStreamStats(
        streamId: $checkedConvert('streamId', (v) => v as String?),
        mode: $checkedConvert('mode', (v) => v as String?),
        kind: $checkedConvert('kind', (v) => v as String?),
        items: $checkedConvert('items', (v) => (v as num?)?.toInt()),
        rejected: $checkedConvert('rejected', (v) => (v as num?)?.toInt()),
        lastItemAt: $checkedConvert(
          'lastItemAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        bindings: $checkedConvert('bindings', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$ExternalStreamStatsToJson(
  ExternalStreamStats instance,
) => <String, dynamic>{
  'streamId': ?instance.streamId,
  'mode': ?instance.mode,
  'kind': ?instance.kind,
  'items': ?instance.items,
  'rejected': ?instance.rejected,
  'lastItemAt': ?instance.lastItemAt?.toIso8601String(),
  'bindings': ?instance.bindings,
};
