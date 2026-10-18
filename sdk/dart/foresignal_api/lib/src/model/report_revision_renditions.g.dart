// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'report_revision_renditions.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReportRevisionRenditionsCWProxy {
  ReportRevisionRenditions markdown(String? markdown);

  ReportRevisionRenditions html(String? html);

  ReportRevisionRenditions json(String? json);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportRevisionRenditions(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportRevisionRenditions(...).copyWith(id: 12, name: "My name")
  /// ```
  ReportRevisionRenditions call({String? markdown, String? html, String? json});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReportRevisionRenditions.copyWith(...)` or call `instanceOfReportRevisionRenditions.copyWith.fieldName(value)` for a single field.
class _$ReportRevisionRenditionsCWProxyImpl
    implements _$ReportRevisionRenditionsCWProxy {
  const _$ReportRevisionRenditionsCWProxyImpl(this._value);

  final ReportRevisionRenditions _value;

  @override
  ReportRevisionRenditions markdown(String? markdown) =>
      call(markdown: markdown);

  @override
  ReportRevisionRenditions html(String? html) => call(html: html);

  @override
  ReportRevisionRenditions json(String? json) => call(json: json);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportRevisionRenditions(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportRevisionRenditions(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReportRevisionRenditions call({
    Object? markdown = const $CopyWithPlaceholder(),
    Object? html = const $CopyWithPlaceholder(),
    Object? json = const $CopyWithPlaceholder(),
  }) {
    return ReportRevisionRenditions(
      markdown: markdown == const $CopyWithPlaceholder()
          ? _value.markdown
          // ignore: cast_nullable_to_non_nullable
          : markdown as String?,
      html: html == const $CopyWithPlaceholder()
          ? _value.html
          // ignore: cast_nullable_to_non_nullable
          : html as String?,
      json: json == const $CopyWithPlaceholder()
          ? _value.json
          // ignore: cast_nullable_to_non_nullable
          : json as String?,
    );
  }
}

extension $ReportRevisionRenditionsCopyWith on ReportRevisionRenditions {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReportRevisionRenditions.copyWith(...)` or `instanceOfReportRevisionRenditions.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReportRevisionRenditionsCWProxy get copyWith =>
      _$ReportRevisionRenditionsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReportRevisionRenditions _$ReportRevisionRenditionsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ReportRevisionRenditions', json, ($checkedConvert) {
  final val = ReportRevisionRenditions(
    markdown: $checkedConvert('markdown', (v) => v as String?),
    html: $checkedConvert('html', (v) => v as String?),
    json: $checkedConvert('json', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ReportRevisionRenditionsToJson(
  ReportRevisionRenditions instance,
) => <String, dynamic>{
  'markdown': ?instance.markdown,
  'html': ?instance.html,
  'json': ?instance.json,
};
