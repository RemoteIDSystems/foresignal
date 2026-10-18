// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_comparison_contradiction.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialComparisonContradictionCWProxy {
  SocialComparisonContradiction reason(
    SocialComparisonContradictionReasonEnum reason,
  );

  SocialComparisonContradiction ref(String ref);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialComparisonContradiction(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialComparisonContradiction(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialComparisonContradiction call({
    SocialComparisonContradictionReasonEnum reason,
    String ref,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialComparisonContradiction.copyWith(...)` or call `instanceOfSocialComparisonContradiction.copyWith.fieldName(value)` for a single field.
class _$SocialComparisonContradictionCWProxyImpl
    implements _$SocialComparisonContradictionCWProxy {
  const _$SocialComparisonContradictionCWProxyImpl(this._value);

  final SocialComparisonContradiction _value;

  @override
  SocialComparisonContradiction reason(
    SocialComparisonContradictionReasonEnum reason,
  ) => call(reason: reason);

  @override
  SocialComparisonContradiction ref(String ref) => call(ref: ref);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialComparisonContradiction(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialComparisonContradiction(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialComparisonContradiction call({
    Object? reason = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
  }) {
    return SocialComparisonContradiction(
      reason: reason == const $CopyWithPlaceholder() || reason == null
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as SocialComparisonContradictionReasonEnum,
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
    );
  }
}

extension $SocialComparisonContradictionCopyWith
    on SocialComparisonContradiction {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialComparisonContradiction.copyWith(...)` or `instanceOfSocialComparisonContradiction.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialComparisonContradictionCWProxy get copyWith =>
      _$SocialComparisonContradictionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialComparisonContradiction _$SocialComparisonContradictionFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SocialComparisonContradiction', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['reason', 'ref']);
  final val = SocialComparisonContradiction(
    reason: $checkedConvert(
      'reason',
      (v) => $enumDecode(_$SocialComparisonContradictionReasonEnumEnumMap, v),
    ),
    ref: $checkedConvert('ref', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$SocialComparisonContradictionToJson(
  SocialComparisonContradiction instance,
) => <String, dynamic>{
  'reason': _$SocialComparisonContradictionReasonEnumEnumMap[instance.reason]!,
  'ref': instance.ref,
};

const _$SocialComparisonContradictionReasonEnumEnumMap = {
  SocialComparisonContradictionReasonEnum.negatingRecord: 'negating-record',
  SocialComparisonContradictionReasonEnum.mediaReuse: 'media-reuse',
};
