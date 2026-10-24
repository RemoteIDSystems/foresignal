// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analysis_request_submission.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalysisRequestSubmissionCWProxy {
  AnalysisRequestSubmission forecasterId(String forecasterId);

  AnalysisRequestSubmission questionId(String questionId);

  AnalysisRequestSubmission orientation(
    AnalysisRequestSubmissionOrientationEnum? orientation,
  );

  AnalysisRequestSubmission horizon(String? horizon);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalysisRequestSubmission(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalysisRequestSubmission(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalysisRequestSubmission call({
    String forecasterId,
    String questionId,
    AnalysisRequestSubmissionOrientationEnum? orientation,
    String? horizon,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalysisRequestSubmission.copyWith(...)` or call `instanceOfAnalysisRequestSubmission.copyWith.fieldName(value)` for a single field.
class _$AnalysisRequestSubmissionCWProxyImpl
    implements _$AnalysisRequestSubmissionCWProxy {
  const _$AnalysisRequestSubmissionCWProxyImpl(this._value);

  final AnalysisRequestSubmission _value;

  @override
  AnalysisRequestSubmission forecasterId(String forecasterId) =>
      call(forecasterId: forecasterId);

  @override
  AnalysisRequestSubmission questionId(String questionId) =>
      call(questionId: questionId);

  @override
  AnalysisRequestSubmission orientation(
    AnalysisRequestSubmissionOrientationEnum? orientation,
  ) => call(orientation: orientation);

  @override
  AnalysisRequestSubmission horizon(String? horizon) => call(horizon: horizon);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalysisRequestSubmission(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalysisRequestSubmission(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalysisRequestSubmission call({
    Object? forecasterId = const $CopyWithPlaceholder(),
    Object? questionId = const $CopyWithPlaceholder(),
    Object? orientation = const $CopyWithPlaceholder(),
    Object? horizon = const $CopyWithPlaceholder(),
  }) {
    return AnalysisRequestSubmission(
      forecasterId:
          forecasterId == const $CopyWithPlaceholder() || forecasterId == null
          ? _value.forecasterId
          // ignore: cast_nullable_to_non_nullable
          : forecasterId as String,
      questionId:
          questionId == const $CopyWithPlaceholder() || questionId == null
          ? _value.questionId
          // ignore: cast_nullable_to_non_nullable
          : questionId as String,
      orientation: orientation == const $CopyWithPlaceholder()
          ? _value.orientation
          // ignore: cast_nullable_to_non_nullable
          : orientation as AnalysisRequestSubmissionOrientationEnum?,
      horizon: horizon == const $CopyWithPlaceholder()
          ? _value.horizon
          // ignore: cast_nullable_to_non_nullable
          : horizon as String?,
    );
  }
}

extension $AnalysisRequestSubmissionCopyWith on AnalysisRequestSubmission {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalysisRequestSubmission.copyWith(...)` or `instanceOfAnalysisRequestSubmission.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalysisRequestSubmissionCWProxy get copyWith =>
      _$AnalysisRequestSubmissionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalysisRequestSubmission _$AnalysisRequestSubmissionFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AnalysisRequestSubmission', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['forecasterId', 'questionId']);
  final val = AnalysisRequestSubmission(
    forecasterId: $checkedConvert('forecasterId', (v) => v as String),
    questionId: $checkedConvert('questionId', (v) => v as String),
    orientation: $checkedConvert(
      'orientation',
      (v) => $enumDecodeNullable(
        _$AnalysisRequestSubmissionOrientationEnumEnumMap,
        v,
      ),
    ),
    horizon: $checkedConvert('horizon', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$AnalysisRequestSubmissionToJson(
  AnalysisRequestSubmission instance,
) => <String, dynamic>{
  'forecasterId': instance.forecasterId,
  'questionId': instance.questionId,
  'orientation':
      ?_$AnalysisRequestSubmissionOrientationEnumEnumMap[instance.orientation],
  'horizon': ?instance.horizon,
};

const _$AnalysisRequestSubmissionOrientationEnumEnumMap = {
  AnalysisRequestSubmissionOrientationEnum.same: 'same',
  AnalysisRequestSubmissionOrientationEnum.inverse: 'inverse',
};
