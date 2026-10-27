// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'retention_run_result_db_cap.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RetentionRunResultDbCapCWProxy {
  RetentionRunResultDbCap capBytes(int? capBytes);

  RetentionRunResultDbCap sizeBeforeBytes(int? sizeBeforeBytes);

  RetentionRunResultDbCap sizeAfterBytes(int? sizeAfterBytes);

  RetentionRunResultDbCap bytesReclaimed(int? bytesReclaimed);

  RetentionRunResultDbCap windowDays(int? windowDays);

  RetentionRunResultDbCap steps(int? steps);

  RetentionRunResultDbCap action(String? action);

  RetentionRunResultDbCap deleted(Map<String, int>? deleted);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RetentionRunResultDbCap(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RetentionRunResultDbCap(...).copyWith(id: 12, name: "My name")
  /// ```
  RetentionRunResultDbCap call({
    int? capBytes,
    int? sizeBeforeBytes,
    int? sizeAfterBytes,
    int? bytesReclaimed,
    int? windowDays,
    int? steps,
    String? action,
    Map<String, int>? deleted,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRetentionRunResultDbCap.copyWith(...)` or call `instanceOfRetentionRunResultDbCap.copyWith.fieldName(value)` for a single field.
class _$RetentionRunResultDbCapCWProxyImpl
    implements _$RetentionRunResultDbCapCWProxy {
  const _$RetentionRunResultDbCapCWProxyImpl(this._value);

  final RetentionRunResultDbCap _value;

  @override
  RetentionRunResultDbCap capBytes(int? capBytes) => call(capBytes: capBytes);

  @override
  RetentionRunResultDbCap sizeBeforeBytes(int? sizeBeforeBytes) =>
      call(sizeBeforeBytes: sizeBeforeBytes);

  @override
  RetentionRunResultDbCap sizeAfterBytes(int? sizeAfterBytes) =>
      call(sizeAfterBytes: sizeAfterBytes);

  @override
  RetentionRunResultDbCap bytesReclaimed(int? bytesReclaimed) =>
      call(bytesReclaimed: bytesReclaimed);

  @override
  RetentionRunResultDbCap windowDays(int? windowDays) =>
      call(windowDays: windowDays);

  @override
  RetentionRunResultDbCap steps(int? steps) => call(steps: steps);

  @override
  RetentionRunResultDbCap action(String? action) => call(action: action);

  @override
  RetentionRunResultDbCap deleted(Map<String, int>? deleted) =>
      call(deleted: deleted);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RetentionRunResultDbCap(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RetentionRunResultDbCap(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RetentionRunResultDbCap call({
    Object? capBytes = const $CopyWithPlaceholder(),
    Object? sizeBeforeBytes = const $CopyWithPlaceholder(),
    Object? sizeAfterBytes = const $CopyWithPlaceholder(),
    Object? bytesReclaimed = const $CopyWithPlaceholder(),
    Object? windowDays = const $CopyWithPlaceholder(),
    Object? steps = const $CopyWithPlaceholder(),
    Object? action = const $CopyWithPlaceholder(),
    Object? deleted = const $CopyWithPlaceholder(),
  }) {
    return RetentionRunResultDbCap(
      capBytes: capBytes == const $CopyWithPlaceholder()
          ? _value.capBytes
          // ignore: cast_nullable_to_non_nullable
          : capBytes as int?,
      sizeBeforeBytes: sizeBeforeBytes == const $CopyWithPlaceholder()
          ? _value.sizeBeforeBytes
          // ignore: cast_nullable_to_non_nullable
          : sizeBeforeBytes as int?,
      sizeAfterBytes: sizeAfterBytes == const $CopyWithPlaceholder()
          ? _value.sizeAfterBytes
          // ignore: cast_nullable_to_non_nullable
          : sizeAfterBytes as int?,
      bytesReclaimed: bytesReclaimed == const $CopyWithPlaceholder()
          ? _value.bytesReclaimed
          // ignore: cast_nullable_to_non_nullable
          : bytesReclaimed as int?,
      windowDays: windowDays == const $CopyWithPlaceholder()
          ? _value.windowDays
          // ignore: cast_nullable_to_non_nullable
          : windowDays as int?,
      steps: steps == const $CopyWithPlaceholder()
          ? _value.steps
          // ignore: cast_nullable_to_non_nullable
          : steps as int?,
      action: action == const $CopyWithPlaceholder()
          ? _value.action
          // ignore: cast_nullable_to_non_nullable
          : action as String?,
      deleted: deleted == const $CopyWithPlaceholder()
          ? _value.deleted
          // ignore: cast_nullable_to_non_nullable
          : deleted as Map<String, int>?,
    );
  }
}

extension $RetentionRunResultDbCapCopyWith on RetentionRunResultDbCap {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRetentionRunResultDbCap.copyWith(...)` or `instanceOfRetentionRunResultDbCap.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RetentionRunResultDbCapCWProxy get copyWith =>
      _$RetentionRunResultDbCapCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RetentionRunResultDbCap _$RetentionRunResultDbCapFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RetentionRunResultDbCap', json, ($checkedConvert) {
  final val = RetentionRunResultDbCap(
    capBytes: $checkedConvert('capBytes', (v) => (v as num?)?.toInt()),
    sizeBeforeBytes: $checkedConvert(
      'sizeBeforeBytes',
      (v) => (v as num?)?.toInt(),
    ),
    sizeAfterBytes: $checkedConvert(
      'sizeAfterBytes',
      (v) => (v as num?)?.toInt(),
    ),
    bytesReclaimed: $checkedConvert(
      'bytesReclaimed',
      (v) => (v as num?)?.toInt(),
    ),
    windowDays: $checkedConvert('windowDays', (v) => (v as num?)?.toInt()),
    steps: $checkedConvert('steps', (v) => (v as num?)?.toInt()),
    action: $checkedConvert('action', (v) => v as String?),
    deleted: $checkedConvert(
      'deleted',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) => MapEntry(k, (e as num).toInt()),
      ),
    ),
  );
  return val;
});

Map<String, dynamic> _$RetentionRunResultDbCapToJson(
  RetentionRunResultDbCap instance,
) => <String, dynamic>{
  'capBytes': ?instance.capBytes,
  'sizeBeforeBytes': ?instance.sizeBeforeBytes,
  'sizeAfterBytes': ?instance.sizeAfterBytes,
  'bytesReclaimed': ?instance.bytesReclaimed,
  'windowDays': ?instance.windowDays,
  'steps': ?instance.steps,
  'action': ?instance.action,
  'deleted': ?instance.deleted,
};
