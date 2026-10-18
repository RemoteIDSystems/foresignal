// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'source_storage.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SourceStorageCWProxy {
  SourceStorage observationsPerMinute(num? observationsPerMinute);

  SourceStorage versionsPerMinute(num? versionsPerMinute);

  SourceStorage persistRatio(num? persistRatio);

  SourceStorage triggerEdgeShare(num? triggerEdgeShare);

  SourceStorage bytesPerDay(int? bytesPerDay);

  SourceStorage projectedBytes(int? projectedBytes);

  SourceStorage lateLastHour(int? lateLastHour);

  SourceStorage clampedLastHour(int? clampedLastHour);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SourceStorage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SourceStorage(...).copyWith(id: 12, name: "My name")
  /// ```
  SourceStorage call({
    num? observationsPerMinute,
    num? versionsPerMinute,
    num? persistRatio,
    num? triggerEdgeShare,
    int? bytesPerDay,
    int? projectedBytes,
    int? lateLastHour,
    int? clampedLastHour,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSourceStorage.copyWith(...)` or call `instanceOfSourceStorage.copyWith.fieldName(value)` for a single field.
class _$SourceStorageCWProxyImpl implements _$SourceStorageCWProxy {
  const _$SourceStorageCWProxyImpl(this._value);

  final SourceStorage _value;

  @override
  SourceStorage observationsPerMinute(num? observationsPerMinute) =>
      call(observationsPerMinute: observationsPerMinute);

  @override
  SourceStorage versionsPerMinute(num? versionsPerMinute) =>
      call(versionsPerMinute: versionsPerMinute);

  @override
  SourceStorage persistRatio(num? persistRatio) =>
      call(persistRatio: persistRatio);

  @override
  SourceStorage triggerEdgeShare(num? triggerEdgeShare) =>
      call(triggerEdgeShare: triggerEdgeShare);

  @override
  SourceStorage bytesPerDay(int? bytesPerDay) => call(bytesPerDay: bytesPerDay);

  @override
  SourceStorage projectedBytes(int? projectedBytes) =>
      call(projectedBytes: projectedBytes);

  @override
  SourceStorage lateLastHour(int? lateLastHour) =>
      call(lateLastHour: lateLastHour);

  @override
  SourceStorage clampedLastHour(int? clampedLastHour) =>
      call(clampedLastHour: clampedLastHour);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SourceStorage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SourceStorage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SourceStorage call({
    Object? observationsPerMinute = const $CopyWithPlaceholder(),
    Object? versionsPerMinute = const $CopyWithPlaceholder(),
    Object? persistRatio = const $CopyWithPlaceholder(),
    Object? triggerEdgeShare = const $CopyWithPlaceholder(),
    Object? bytesPerDay = const $CopyWithPlaceholder(),
    Object? projectedBytes = const $CopyWithPlaceholder(),
    Object? lateLastHour = const $CopyWithPlaceholder(),
    Object? clampedLastHour = const $CopyWithPlaceholder(),
  }) {
    return SourceStorage(
      observationsPerMinute:
          observationsPerMinute == const $CopyWithPlaceholder()
          ? _value.observationsPerMinute
          // ignore: cast_nullable_to_non_nullable
          : observationsPerMinute as num?,
      versionsPerMinute: versionsPerMinute == const $CopyWithPlaceholder()
          ? _value.versionsPerMinute
          // ignore: cast_nullable_to_non_nullable
          : versionsPerMinute as num?,
      persistRatio: persistRatio == const $CopyWithPlaceholder()
          ? _value.persistRatio
          // ignore: cast_nullable_to_non_nullable
          : persistRatio as num?,
      triggerEdgeShare: triggerEdgeShare == const $CopyWithPlaceholder()
          ? _value.triggerEdgeShare
          // ignore: cast_nullable_to_non_nullable
          : triggerEdgeShare as num?,
      bytesPerDay: bytesPerDay == const $CopyWithPlaceholder()
          ? _value.bytesPerDay
          // ignore: cast_nullable_to_non_nullable
          : bytesPerDay as int?,
      projectedBytes: projectedBytes == const $CopyWithPlaceholder()
          ? _value.projectedBytes
          // ignore: cast_nullable_to_non_nullable
          : projectedBytes as int?,
      lateLastHour: lateLastHour == const $CopyWithPlaceholder()
          ? _value.lateLastHour
          // ignore: cast_nullable_to_non_nullable
          : lateLastHour as int?,
      clampedLastHour: clampedLastHour == const $CopyWithPlaceholder()
          ? _value.clampedLastHour
          // ignore: cast_nullable_to_non_nullable
          : clampedLastHour as int?,
    );
  }
}

extension $SourceStorageCopyWith on SourceStorage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSourceStorage.copyWith(...)` or `instanceOfSourceStorage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SourceStorageCWProxy get copyWith => _$SourceStorageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SourceStorage _$SourceStorageFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SourceStorage', json, ($checkedConvert) {
  final val = SourceStorage(
    observationsPerMinute: $checkedConvert(
      'observationsPerMinute',
      (v) => v as num?,
    ),
    versionsPerMinute: $checkedConvert('versionsPerMinute', (v) => v as num?),
    persistRatio: $checkedConvert('persistRatio', (v) => v as num?),
    triggerEdgeShare: $checkedConvert('triggerEdgeShare', (v) => v as num?),
    bytesPerDay: $checkedConvert('bytesPerDay', (v) => (v as num?)?.toInt()),
    projectedBytes: $checkedConvert(
      'projectedBytes',
      (v) => (v as num?)?.toInt(),
    ),
    lateLastHour: $checkedConvert('lateLastHour', (v) => (v as num?)?.toInt()),
    clampedLastHour: $checkedConvert(
      'clampedLastHour',
      (v) => (v as num?)?.toInt(),
    ),
  );
  return val;
});

Map<String, dynamic> _$SourceStorageToJson(SourceStorage instance) =>
    <String, dynamic>{
      'observationsPerMinute': ?instance.observationsPerMinute,
      'versionsPerMinute': ?instance.versionsPerMinute,
      'persistRatio': ?instance.persistRatio,
      'triggerEdgeShare': ?instance.triggerEdgeShare,
      'bytesPerDay': ?instance.bytesPerDay,
      'projectedBytes': ?instance.projectedBytes,
      'lateLastHour': ?instance.lateLastHour,
      'clampedLastHour': ?instance.clampedLastHour,
    };
