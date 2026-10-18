// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'precursor_patch.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrecursorPatchCWProxy {
  PrecursorPatch status(PrecursorPatchStatusEnum status);

  PrecursorPatch reason(String reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  PrecursorPatch call({PrecursorPatchStatusEnum status, String reason});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrecursorPatch.copyWith(...)` or call `instanceOfPrecursorPatch.copyWith.fieldName(value)` for a single field.
class _$PrecursorPatchCWProxyImpl implements _$PrecursorPatchCWProxy {
  const _$PrecursorPatchCWProxyImpl(this._value);

  final PrecursorPatch _value;

  @override
  PrecursorPatch status(PrecursorPatchStatusEnum status) =>
      call(status: status);

  @override
  PrecursorPatch reason(String reason) => call(reason: reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrecursorPatch call({
    Object? status = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
  }) {
    return PrecursorPatch(
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as PrecursorPatchStatusEnum,
      reason: reason == const $CopyWithPlaceholder() || reason == null
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String,
    );
  }
}

extension $PrecursorPatchCopyWith on PrecursorPatch {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrecursorPatch.copyWith(...)` or `instanceOfPrecursorPatch.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrecursorPatchCWProxy get copyWith => _$PrecursorPatchCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrecursorPatch _$PrecursorPatchFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PrecursorPatch', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['status', 'reason']);
      final val = PrecursorPatch(
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$PrecursorPatchStatusEnumEnumMap, v),
        ),
        reason: $checkedConvert('reason', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$PrecursorPatchToJson(PrecursorPatch instance) =>
    <String, dynamic>{
      'status': _$PrecursorPatchStatusEnumEnumMap[instance.status]!,
      'reason': instance.reason,
    };

const _$PrecursorPatchStatusEnumEnumMap = {
  PrecursorPatchStatusEnum.retired: 'retired',
  PrecursorPatchStatusEnum.validated: 'validated',
};
