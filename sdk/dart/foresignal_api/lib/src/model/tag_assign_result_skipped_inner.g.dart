// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tag_assign_result_skipped_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TagAssignResultSkippedInnerCWProxy {
  TagAssignResultSkippedInner ref(String ref);

  TagAssignResultSkippedInner reason(
    TagAssignResultSkippedInnerReasonEnum reason,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TagAssignResultSkippedInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TagAssignResultSkippedInner(...).copyWith(id: 12, name: "My name")
  /// ```
  TagAssignResultSkippedInner call({
    String ref,
    TagAssignResultSkippedInnerReasonEnum reason,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTagAssignResultSkippedInner.copyWith(...)` or call `instanceOfTagAssignResultSkippedInner.copyWith.fieldName(value)` for a single field.
class _$TagAssignResultSkippedInnerCWProxyImpl
    implements _$TagAssignResultSkippedInnerCWProxy {
  const _$TagAssignResultSkippedInnerCWProxyImpl(this._value);

  final TagAssignResultSkippedInner _value;

  @override
  TagAssignResultSkippedInner ref(String ref) => call(ref: ref);

  @override
  TagAssignResultSkippedInner reason(
    TagAssignResultSkippedInnerReasonEnum reason,
  ) => call(reason: reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TagAssignResultSkippedInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TagAssignResultSkippedInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TagAssignResultSkippedInner call({
    Object? ref = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
  }) {
    return TagAssignResultSkippedInner(
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      reason: reason == const $CopyWithPlaceholder() || reason == null
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as TagAssignResultSkippedInnerReasonEnum,
    );
  }
}

extension $TagAssignResultSkippedInnerCopyWith on TagAssignResultSkippedInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTagAssignResultSkippedInner.copyWith(...)` or `instanceOfTagAssignResultSkippedInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TagAssignResultSkippedInnerCWProxy get copyWith =>
      _$TagAssignResultSkippedInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TagAssignResultSkippedInner _$TagAssignResultSkippedInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('TagAssignResultSkippedInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['ref', 'reason']);
  final val = TagAssignResultSkippedInner(
    ref: $checkedConvert('ref', (v) => v as String),
    reason: $checkedConvert(
      'reason',
      (v) => $enumDecode(_$TagAssignResultSkippedInnerReasonEnumEnumMap, v),
    ),
  );
  return val;
});

Map<String, dynamic> _$TagAssignResultSkippedInnerToJson(
  TagAssignResultSkippedInner instance,
) => <String, dynamic>{
  'ref': instance.ref,
  'reason': _$TagAssignResultSkippedInnerReasonEnumEnumMap[instance.reason]!,
};

const _$TagAssignResultSkippedInnerReasonEnumEnumMap = {
  TagAssignResultSkippedInnerReasonEnum.notVisible: 'not-visible',
  TagAssignResultSkippedInnerReasonEnum.invalidRef: 'invalid-ref',
};
