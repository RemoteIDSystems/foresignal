// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'report_draft_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReportDraftRequestCWProxy {
  ReportDraftRequest source_(ReportSource source_);

  ReportDraftRequest template(ReportDraftRequestTemplateEnum? template);

  ReportDraftRequest title(String? title);

  ReportDraftRequest timeWindow(TimeRange? timeWindow);

  ReportDraftRequest area(Geometry? area);

  ReportDraftRequest narrative(bool? narrative);

  ReportDraftRequest assessmentId(String? assessmentId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportDraftRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportDraftRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  ReportDraftRequest call({
    ReportSource source_,
    ReportDraftRequestTemplateEnum? template,
    String? title,
    TimeRange? timeWindow,
    Geometry? area,
    bool? narrative,
    String? assessmentId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReportDraftRequest.copyWith(...)` or call `instanceOfReportDraftRequest.copyWith.fieldName(value)` for a single field.
class _$ReportDraftRequestCWProxyImpl implements _$ReportDraftRequestCWProxy {
  const _$ReportDraftRequestCWProxyImpl(this._value);

  final ReportDraftRequest _value;

  @override
  ReportDraftRequest source_(ReportSource source_) => call(source_: source_);

  @override
  ReportDraftRequest template(ReportDraftRequestTemplateEnum? template) =>
      call(template: template);

  @override
  ReportDraftRequest title(String? title) => call(title: title);

  @override
  ReportDraftRequest timeWindow(TimeRange? timeWindow) =>
      call(timeWindow: timeWindow);

  @override
  ReportDraftRequest area(Geometry? area) => call(area: area);

  @override
  ReportDraftRequest narrative(bool? narrative) => call(narrative: narrative);

  @override
  ReportDraftRequest assessmentId(String? assessmentId) =>
      call(assessmentId: assessmentId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportDraftRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportDraftRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReportDraftRequest call({
    Object? source_ = const $CopyWithPlaceholder(),
    Object? template = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? timeWindow = const $CopyWithPlaceholder(),
    Object? area = const $CopyWithPlaceholder(),
    Object? narrative = const $CopyWithPlaceholder(),
    Object? assessmentId = const $CopyWithPlaceholder(),
  }) {
    return ReportDraftRequest(
      source_: source_ == const $CopyWithPlaceholder() || source_ == null
          ? _value.source_
          // ignore: cast_nullable_to_non_nullable
          : source_ as ReportSource,
      template: template == const $CopyWithPlaceholder()
          ? _value.template
          // ignore: cast_nullable_to_non_nullable
          : template as ReportDraftRequestTemplateEnum?,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      timeWindow: timeWindow == const $CopyWithPlaceholder()
          ? _value.timeWindow
          // ignore: cast_nullable_to_non_nullable
          : timeWindow as TimeRange?,
      area: area == const $CopyWithPlaceholder()
          ? _value.area
          // ignore: cast_nullable_to_non_nullable
          : area as Geometry?,
      narrative: narrative == const $CopyWithPlaceholder()
          ? _value.narrative
          // ignore: cast_nullable_to_non_nullable
          : narrative as bool?,
      assessmentId: assessmentId == const $CopyWithPlaceholder()
          ? _value.assessmentId
          // ignore: cast_nullable_to_non_nullable
          : assessmentId as String?,
    );
  }
}

extension $ReportDraftRequestCopyWith on ReportDraftRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReportDraftRequest.copyWith(...)` or `instanceOfReportDraftRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReportDraftRequestCWProxy get copyWith =>
      _$ReportDraftRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReportDraftRequest _$ReportDraftRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ReportDraftRequest', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['source']);
      final val = ReportDraftRequest(
        source_: $checkedConvert(
          'source',
          (v) => ReportSource.fromJson(v as Map<String, dynamic>),
        ),
        template: $checkedConvert(
          'template',
          (v) =>
              $enumDecodeNullable(_$ReportDraftRequestTemplateEnumEnumMap, v),
        ),
        title: $checkedConvert('title', (v) => v as String?),
        timeWindow: $checkedConvert(
          'timeWindow',
          (v) =>
              v == null ? null : TimeRange.fromJson(v as Map<String, dynamic>),
        ),
        area: $checkedConvert(
          'area',
          (v) =>
              v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
        ),
        narrative: $checkedConvert('narrative', (v) => v as bool? ?? false),
        assessmentId: $checkedConvert('assessmentId', (v) => v as String?),
      );
      return val;
    }, fieldKeyMap: const {'source_': 'source'});

Map<String, dynamic> _$ReportDraftRequestToJson(ReportDraftRequest instance) =>
    <String, dynamic>{
      'source': instance.source_.toJson(),
      'template': ?_$ReportDraftRequestTemplateEnumEnumMap[instance.template],
      'title': ?instance.title,
      'timeWindow': ?instance.timeWindow?.toJson(),
      'area': ?instance.area?.toJson(),
      'narrative': ?instance.narrative,
      'assessmentId': ?instance.assessmentId,
    };

const _$ReportDraftRequestTemplateEnumEnumMap = {
  ReportDraftRequestTemplateEnum.situationBrief: 'situation-brief',
  ReportDraftRequestTemplateEnum.investigationSummary: 'investigation-summary',
  ReportDraftRequestTemplateEnum.chronicleRecap: 'chronicle-recap',
  ReportDraftRequestTemplateEnum.weeklyAssessment: 'weekly-assessment',
  ReportDraftRequestTemplateEnum.custom: 'custom',
};
