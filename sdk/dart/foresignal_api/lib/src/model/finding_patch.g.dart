// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'finding_patch.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FindingPatchCWProxy {
  FindingPatch status(FindingPatchStatusEnum status);

  FindingPatch reason(String reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FindingPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FindingPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  FindingPatch call({FindingPatchStatusEnum status, String reason});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFindingPatch.copyWith(...)` or call `instanceOfFindingPatch.copyWith.fieldName(value)` for a single field.
class _$FindingPatchCWProxyImpl implements _$FindingPatchCWProxy {
  const _$FindingPatchCWProxyImpl(this._value);

  final FindingPatch _value;

  @override
  FindingPatch status(FindingPatchStatusEnum status) => call(status: status);

  @override
  FindingPatch reason(String reason) => call(reason: reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FindingPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FindingPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FindingPatch call({
    Object? status = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
  }) {
    return FindingPatch(
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as FindingPatchStatusEnum,
      reason: reason == const $CopyWithPlaceholder() || reason == null
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String,
    );
  }
}

extension $FindingPatchCopyWith on FindingPatch {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFindingPatch.copyWith(...)` or `instanceOfFindingPatch.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FindingPatchCWProxy get copyWith => _$FindingPatchCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FindingPatch _$FindingPatchFromJson(Map<String, dynamic> json) =>
    $checkedCreate('FindingPatch', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['status', 'reason']);
      final val = FindingPatch(
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$FindingPatchStatusEnumEnumMap, v),
        ),
        reason: $checkedConvert('reason', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$FindingPatchToJson(FindingPatch instance) =>
    <String, dynamic>{
      'status': _$FindingPatchStatusEnumEnumMap[instance.status]!,
      'reason': instance.reason,
    };

const _$FindingPatchStatusEnumEnumMap = {
  FindingPatchStatusEnum.dismissed: 'dismissed',
};
