// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'infra_impact_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$InfraImpactResultCWProxy {
  InfraImpactResult buildId(String buildId);

  InfraImpactResult approximateArea(bool? approximateArea);

  InfraImpactResult direct(List<InfraNode> direct);

  InfraImpactResult dependents(
    List<InfraImpactResultDependentsInner> dependents,
  );

  InfraImpactResult summary(InfraImpactResultSummary summary);

  InfraImpactResult caveats(List<Caveat> caveats);

  InfraImpactResult attribution(List<String> attribution);

  InfraImpactResult truncated(bool truncated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InfraImpactResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InfraImpactResult(...).copyWith(id: 12, name: "My name")
  /// ```
  InfraImpactResult call({
    String buildId,
    bool? approximateArea,
    List<InfraNode> direct,
    List<InfraImpactResultDependentsInner> dependents,
    InfraImpactResultSummary summary,
    List<Caveat> caveats,
    List<String> attribution,
    bool truncated,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfInfraImpactResult.copyWith(...)` or call `instanceOfInfraImpactResult.copyWith.fieldName(value)` for a single field.
class _$InfraImpactResultCWProxyImpl implements _$InfraImpactResultCWProxy {
  const _$InfraImpactResultCWProxyImpl(this._value);

  final InfraImpactResult _value;

  @override
  InfraImpactResult buildId(String buildId) => call(buildId: buildId);

  @override
  InfraImpactResult approximateArea(bool? approximateArea) =>
      call(approximateArea: approximateArea);

  @override
  InfraImpactResult direct(List<InfraNode> direct) => call(direct: direct);

  @override
  InfraImpactResult dependents(
    List<InfraImpactResultDependentsInner> dependents,
  ) => call(dependents: dependents);

  @override
  InfraImpactResult summary(InfraImpactResultSummary summary) =>
      call(summary: summary);

  @override
  InfraImpactResult caveats(List<Caveat> caveats) => call(caveats: caveats);

  @override
  InfraImpactResult attribution(List<String> attribution) =>
      call(attribution: attribution);

  @override
  InfraImpactResult truncated(bool truncated) => call(truncated: truncated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InfraImpactResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InfraImpactResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  InfraImpactResult call({
    Object? buildId = const $CopyWithPlaceholder(),
    Object? approximateArea = const $CopyWithPlaceholder(),
    Object? direct = const $CopyWithPlaceholder(),
    Object? dependents = const $CopyWithPlaceholder(),
    Object? summary = const $CopyWithPlaceholder(),
    Object? caveats = const $CopyWithPlaceholder(),
    Object? attribution = const $CopyWithPlaceholder(),
    Object? truncated = const $CopyWithPlaceholder(),
  }) {
    return InfraImpactResult(
      buildId: buildId == const $CopyWithPlaceholder() || buildId == null
          ? _value.buildId
          // ignore: cast_nullable_to_non_nullable
          : buildId as String,
      approximateArea: approximateArea == const $CopyWithPlaceholder()
          ? _value.approximateArea
          // ignore: cast_nullable_to_non_nullable
          : approximateArea as bool?,
      direct: direct == const $CopyWithPlaceholder() || direct == null
          ? _value.direct
          // ignore: cast_nullable_to_non_nullable
          : direct as List<InfraNode>,
      dependents:
          dependents == const $CopyWithPlaceholder() || dependents == null
          ? _value.dependents
          // ignore: cast_nullable_to_non_nullable
          : dependents as List<InfraImpactResultDependentsInner>,
      summary: summary == const $CopyWithPlaceholder() || summary == null
          ? _value.summary
          // ignore: cast_nullable_to_non_nullable
          : summary as InfraImpactResultSummary,
      caveats: caveats == const $CopyWithPlaceholder() || caveats == null
          ? _value.caveats
          // ignore: cast_nullable_to_non_nullable
          : caveats as List<Caveat>,
      attribution:
          attribution == const $CopyWithPlaceholder() || attribution == null
          ? _value.attribution
          // ignore: cast_nullable_to_non_nullable
          : attribution as List<String>,
      truncated: truncated == const $CopyWithPlaceholder() || truncated == null
          ? _value.truncated
          // ignore: cast_nullable_to_non_nullable
          : truncated as bool,
    );
  }
}

extension $InfraImpactResultCopyWith on InfraImpactResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfInfraImpactResult.copyWith(...)` or `instanceOfInfraImpactResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$InfraImpactResultCWProxy get copyWith =>
      _$InfraImpactResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

InfraImpactResult _$InfraImpactResultFromJson(Map<String, dynamic> json) =>
    $checkedCreate('InfraImpactResult', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'buildId',
          'direct',
          'dependents',
          'summary',
          'caveats',
          'attribution',
          'truncated',
        ],
      );
      final val = InfraImpactResult(
        buildId: $checkedConvert('buildId', (v) => v as String),
        approximateArea: $checkedConvert('approximateArea', (v) => v as bool?),
        direct: $checkedConvert(
          'direct',
          (v) => (v as List<dynamic>)
              .map((e) => InfraNode.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        dependents: $checkedConvert(
          'dependents',
          (v) => (v as List<dynamic>)
              .map(
                (e) => InfraImpactResultDependentsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        summary: $checkedConvert(
          'summary',
          (v) => InfraImpactResultSummary.fromJson(v as Map<String, dynamic>),
        ),
        caveats: $checkedConvert(
          'caveats',
          (v) => (v as List<dynamic>)
              .map((e) => Caveat.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        attribution: $checkedConvert(
          'attribution',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
        truncated: $checkedConvert('truncated', (v) => v as bool),
      );
      return val;
    });

Map<String, dynamic> _$InfraImpactResultToJson(InfraImpactResult instance) =>
    <String, dynamic>{
      'buildId': instance.buildId,
      'approximateArea': ?instance.approximateArea,
      'direct': instance.direct.map((e) => e.toJson()).toList(),
      'dependents': instance.dependents.map((e) => e.toJson()).toList(),
      'summary': instance.summary.toJson(),
      'caveats': instance.caveats.map((e) => e.toJson()).toList(),
      'attribution': instance.attribution,
      'truncated': instance.truncated,
    };
