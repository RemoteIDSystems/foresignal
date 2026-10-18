// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hypothesis_review_input_assumptions_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HypothesisReviewInputAssumptionsInnerCWProxy {
  HypothesisReviewInputAssumptionsInner assumptionId(String assumptionId);

  HypothesisReviewInputAssumptionsInner status(
    HypothesisReviewInputAssumptionsInnerStatusEnum status,
  );

  HypothesisReviewInputAssumptionsInner rationale(String? rationale);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisReviewInputAssumptionsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisReviewInputAssumptionsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  HypothesisReviewInputAssumptionsInner call({
    String assumptionId,
    HypothesisReviewInputAssumptionsInnerStatusEnum status,
    String? rationale,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHypothesisReviewInputAssumptionsInner.copyWith(...)` or call `instanceOfHypothesisReviewInputAssumptionsInner.copyWith.fieldName(value)` for a single field.
class _$HypothesisReviewInputAssumptionsInnerCWProxyImpl
    implements _$HypothesisReviewInputAssumptionsInnerCWProxy {
  const _$HypothesisReviewInputAssumptionsInnerCWProxyImpl(this._value);

  final HypothesisReviewInputAssumptionsInner _value;

  @override
  HypothesisReviewInputAssumptionsInner assumptionId(String assumptionId) =>
      call(assumptionId: assumptionId);

  @override
  HypothesisReviewInputAssumptionsInner status(
    HypothesisReviewInputAssumptionsInnerStatusEnum status,
  ) => call(status: status);

  @override
  HypothesisReviewInputAssumptionsInner rationale(String? rationale) =>
      call(rationale: rationale);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisReviewInputAssumptionsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisReviewInputAssumptionsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HypothesisReviewInputAssumptionsInner call({
    Object? assumptionId = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? rationale = const $CopyWithPlaceholder(),
  }) {
    return HypothesisReviewInputAssumptionsInner(
      assumptionId:
          assumptionId == const $CopyWithPlaceholder() || assumptionId == null
          ? _value.assumptionId
          // ignore: cast_nullable_to_non_nullable
          : assumptionId as String,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as HypothesisReviewInputAssumptionsInnerStatusEnum,
      rationale: rationale == const $CopyWithPlaceholder()
          ? _value.rationale
          // ignore: cast_nullable_to_non_nullable
          : rationale as String?,
    );
  }
}

extension $HypothesisReviewInputAssumptionsInnerCopyWith
    on HypothesisReviewInputAssumptionsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHypothesisReviewInputAssumptionsInner.copyWith(...)` or `instanceOfHypothesisReviewInputAssumptionsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HypothesisReviewInputAssumptionsInnerCWProxy get copyWith =>
      _$HypothesisReviewInputAssumptionsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HypothesisReviewInputAssumptionsInner
_$HypothesisReviewInputAssumptionsInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('HypothesisReviewInputAssumptionsInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['assumptionId', 'status']);
      final val = HypothesisReviewInputAssumptionsInner(
        assumptionId: $checkedConvert('assumptionId', (v) => v as String),
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(
            _$HypothesisReviewInputAssumptionsInnerStatusEnumEnumMap,
            v,
          ),
        ),
        rationale: $checkedConvert('rationale', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$HypothesisReviewInputAssumptionsInnerToJson(
  HypothesisReviewInputAssumptionsInner instance,
) => <String, dynamic>{
  'assumptionId': instance.assumptionId,
  'status':
      _$HypothesisReviewInputAssumptionsInnerStatusEnumEnumMap[instance
          .status]!,
  'rationale': ?instance.rationale,
};

const _$HypothesisReviewInputAssumptionsInnerStatusEnumEnumMap = {
  HypothesisReviewInputAssumptionsInnerStatusEnum.solid: 'solid',
  HypothesisReviewInputAssumptionsInnerStatusEnum.caveated: 'caveated',
  HypothesisReviewInputAssumptionsInnerStatusEnum.unsupported: 'unsupported',
  HypothesisReviewInputAssumptionsInnerStatusEnum.broken: 'broken',
};
