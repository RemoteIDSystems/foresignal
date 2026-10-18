// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'situation_assessment_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SituationAssessmentRequestCWProxy {
  SituationAssessmentRequest roles(
    Set<SituationAssessmentRequestRolesEnum>? roles,
  );

  SituationAssessmentRequest effort(
    SituationAssessmentRequestEffortEnum? effort,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationAssessmentRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationAssessmentRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  SituationAssessmentRequest call({
    Set<SituationAssessmentRequestRolesEnum>? roles,
    SituationAssessmentRequestEffortEnum? effort,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSituationAssessmentRequest.copyWith(...)` or call `instanceOfSituationAssessmentRequest.copyWith.fieldName(value)` for a single field.
class _$SituationAssessmentRequestCWProxyImpl
    implements _$SituationAssessmentRequestCWProxy {
  const _$SituationAssessmentRequestCWProxyImpl(this._value);

  final SituationAssessmentRequest _value;

  @override
  SituationAssessmentRequest roles(
    Set<SituationAssessmentRequestRolesEnum>? roles,
  ) => call(roles: roles);

  @override
  SituationAssessmentRequest effort(
    SituationAssessmentRequestEffortEnum? effort,
  ) => call(effort: effort);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationAssessmentRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationAssessmentRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SituationAssessmentRequest call({
    Object? roles = const $CopyWithPlaceholder(),
    Object? effort = const $CopyWithPlaceholder(),
  }) {
    return SituationAssessmentRequest(
      roles: roles == const $CopyWithPlaceholder()
          ? _value.roles
          // ignore: cast_nullable_to_non_nullable
          : roles as Set<SituationAssessmentRequestRolesEnum>?,
      effort: effort == const $CopyWithPlaceholder()
          ? _value.effort
          // ignore: cast_nullable_to_non_nullable
          : effort as SituationAssessmentRequestEffortEnum?,
    );
  }
}

extension $SituationAssessmentRequestCopyWith on SituationAssessmentRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSituationAssessmentRequest.copyWith(...)` or `instanceOfSituationAssessmentRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SituationAssessmentRequestCWProxy get copyWith =>
      _$SituationAssessmentRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SituationAssessmentRequest _$SituationAssessmentRequestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SituationAssessmentRequest', json, ($checkedConvert) {
  final val = SituationAssessmentRequest(
    roles: $checkedConvert(
      'roles',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => $enumDecode(_$SituationAssessmentRequestRolesEnumEnumMap, e),
          )
          .toSet(),
    ),
    effort: $checkedConvert(
      'effort',
      (v) =>
          $enumDecodeNullable(_$SituationAssessmentRequestEffortEnumEnumMap, v),
    ),
  );
  return val;
});

Map<String, dynamic> _$SituationAssessmentRequestToJson(
  SituationAssessmentRequest instance,
) => <String, dynamic>{
  'roles': ?instance.roles
      ?.map((e) => _$SituationAssessmentRequestRolesEnumEnumMap[e]!)
      .toList(),
  'effort': ?_$SituationAssessmentRequestEffortEnumEnumMap[instance.effort],
};

const _$SituationAssessmentRequestRolesEnumEnumMap = {
  SituationAssessmentRequestRolesEnum.analyst: 'analyst',
  SituationAssessmentRequestRolesEnum.redTeam: 'red-team',
  SituationAssessmentRequestRolesEnum.forecaster: 'forecaster',
  SituationAssessmentRequestRolesEnum.proposer: 'proposer',
};

const _$SituationAssessmentRequestEffortEnumEnumMap = {
  SituationAssessmentRequestEffortEnum.low: 'low',
  SituationAssessmentRequestEffortEnum.medium: 'medium',
  SituationAssessmentRequestEffortEnum.high: 'high',
};
