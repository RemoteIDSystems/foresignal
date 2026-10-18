// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'report_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReportInputCWProxy {
  ReportInput title(String title);

  ReportInput subtitle(String? subtitle);

  ReportInput authors(List<String>? authors);

  ReportInput tlp(Tlp? tlp);

  ReportInput visibility(Visibility? visibility);

  ReportInput source_(ReportSource? source_);

  ReportInput template(ReportInputTemplateEnum? template);

  ReportInput timeWindow(TimeRange? timeWindow);

  ReportInput area(Geometry? area);

  ReportInput blocks(List<ReportBlock>? blocks);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportInput(...).copyWith(id: 12, name: "My name")
  /// ```
  ReportInput call({
    String title,
    String? subtitle,
    List<String>? authors,
    Tlp? tlp,
    Visibility? visibility,
    ReportSource? source_,
    ReportInputTemplateEnum? template,
    TimeRange? timeWindow,
    Geometry? area,
    List<ReportBlock>? blocks,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReportInput.copyWith(...)` or call `instanceOfReportInput.copyWith.fieldName(value)` for a single field.
class _$ReportInputCWProxyImpl implements _$ReportInputCWProxy {
  const _$ReportInputCWProxyImpl(this._value);

  final ReportInput _value;

  @override
  ReportInput title(String title) => call(title: title);

  @override
  ReportInput subtitle(String? subtitle) => call(subtitle: subtitle);

  @override
  ReportInput authors(List<String>? authors) => call(authors: authors);

  @override
  ReportInput tlp(Tlp? tlp) => call(tlp: tlp);

  @override
  ReportInput visibility(Visibility? visibility) =>
      call(visibility: visibility);

  @override
  ReportInput source_(ReportSource? source_) => call(source_: source_);

  @override
  ReportInput template(ReportInputTemplateEnum? template) =>
      call(template: template);

  @override
  ReportInput timeWindow(TimeRange? timeWindow) => call(timeWindow: timeWindow);

  @override
  ReportInput area(Geometry? area) => call(area: area);

  @override
  ReportInput blocks(List<ReportBlock>? blocks) => call(blocks: blocks);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReportInput call({
    Object? title = const $CopyWithPlaceholder(),
    Object? subtitle = const $CopyWithPlaceholder(),
    Object? authors = const $CopyWithPlaceholder(),
    Object? tlp = const $CopyWithPlaceholder(),
    Object? visibility = const $CopyWithPlaceholder(),
    Object? source_ = const $CopyWithPlaceholder(),
    Object? template = const $CopyWithPlaceholder(),
    Object? timeWindow = const $CopyWithPlaceholder(),
    Object? area = const $CopyWithPlaceholder(),
    Object? blocks = const $CopyWithPlaceholder(),
  }) {
    return ReportInput(
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      subtitle: subtitle == const $CopyWithPlaceholder()
          ? _value.subtitle
          // ignore: cast_nullable_to_non_nullable
          : subtitle as String?,
      authors: authors == const $CopyWithPlaceholder()
          ? _value.authors
          // ignore: cast_nullable_to_non_nullable
          : authors as List<String>?,
      tlp: tlp == const $CopyWithPlaceholder()
          ? _value.tlp
          // ignore: cast_nullable_to_non_nullable
          : tlp as Tlp?,
      visibility: visibility == const $CopyWithPlaceholder()
          ? _value.visibility
          // ignore: cast_nullable_to_non_nullable
          : visibility as Visibility?,
      source_: source_ == const $CopyWithPlaceholder()
          ? _value.source_
          // ignore: cast_nullable_to_non_nullable
          : source_ as ReportSource?,
      template: template == const $CopyWithPlaceholder()
          ? _value.template
          // ignore: cast_nullable_to_non_nullable
          : template as ReportInputTemplateEnum?,
      timeWindow: timeWindow == const $CopyWithPlaceholder()
          ? _value.timeWindow
          // ignore: cast_nullable_to_non_nullable
          : timeWindow as TimeRange?,
      area: area == const $CopyWithPlaceholder()
          ? _value.area
          // ignore: cast_nullable_to_non_nullable
          : area as Geometry?,
      blocks: blocks == const $CopyWithPlaceholder()
          ? _value.blocks
          // ignore: cast_nullable_to_non_nullable
          : blocks as List<ReportBlock>?,
    );
  }
}

extension $ReportInputCopyWith on ReportInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReportInput.copyWith(...)` or `instanceOfReportInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReportInputCWProxy get copyWith => _$ReportInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReportInput _$ReportInputFromJson(Map<String, dynamic> json) => $checkedCreate(
  'ReportInput',
  json,
  ($checkedConvert) {
    $checkKeys(json, requiredKeys: const ['title']);
    final val = ReportInput(
      title: $checkedConvert('title', (v) => v as String),
      subtitle: $checkedConvert('subtitle', (v) => v as String?),
      authors: $checkedConvert(
        'authors',
        (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
      ),
      tlp: $checkedConvert('tlp', (v) => $enumDecodeNullable(_$TlpEnumMap, v)),
      visibility: $checkedConvert(
        'visibility',
        (v) => $enumDecodeNullable(_$VisibilityEnumMap, v),
      ),
      source_: $checkedConvert(
        'source',
        (v) =>
            v == null ? null : ReportSource.fromJson(v as Map<String, dynamic>),
      ),
      template: $checkedConvert(
        'template',
        (v) => $enumDecodeNullable(_$ReportInputTemplateEnumEnumMap, v),
      ),
      timeWindow: $checkedConvert(
        'timeWindow',
        (v) => v == null ? null : TimeRange.fromJson(v as Map<String, dynamic>),
      ),
      area: $checkedConvert(
        'area',
        (v) => v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
      ),
      blocks: $checkedConvert(
        'blocks',
        (v) => (v as List<dynamic>?)
            ?.map((e) => ReportBlock.fromJson(e as Map<String, dynamic>))
            .toList(),
      ),
    );
    return val;
  },
  fieldKeyMap: const {'source_': 'source'},
);

Map<String, dynamic> _$ReportInputToJson(ReportInput instance) =>
    <String, dynamic>{
      'title': instance.title,
      'subtitle': ?instance.subtitle,
      'authors': ?instance.authors,
      'tlp': ?_$TlpEnumMap[instance.tlp],
      'visibility': ?_$VisibilityEnumMap[instance.visibility],
      'source': ?instance.source_?.toJson(),
      'template': ?_$ReportInputTemplateEnumEnumMap[instance.template],
      'timeWindow': ?instance.timeWindow?.toJson(),
      'area': ?instance.area?.toJson(),
      'blocks': ?instance.blocks?.map((e) => e.toJson()).toList(),
    };

const _$TlpEnumMap = {
  Tlp.CLEAR: 'CLEAR',
  Tlp.GREEN: 'GREEN',
  Tlp.AMBER: 'AMBER',
  Tlp.aMBERPlusSTRICT: 'AMBER+STRICT',
  Tlp.RED: 'RED',
};

const _$VisibilityEnumMap = {
  Visibility.private: 'private',
  Visibility.shared: 'shared',
  Visibility.instance: 'instance',
};

const _$ReportInputTemplateEnumEnumMap = {
  ReportInputTemplateEnum.situationBrief: 'situation-brief',
  ReportInputTemplateEnum.investigationSummary: 'investigation-summary',
  ReportInputTemplateEnum.chronicleRecap: 'chronicle-recap',
  ReportInputTemplateEnum.weeklyAssessment: 'weekly-assessment',
  ReportInputTemplateEnum.custom: 'custom',
};
