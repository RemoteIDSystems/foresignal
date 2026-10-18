// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'attribution.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AttributionCWProxy {
  Attribution text(String text);

  Attribution html(String? html);

  Attribution url(String? url);

  Attribution licence(String licence);

  Attribution licenceUrl(String? licenceUrl);

  Attribution requiredDisplay(bool requiredDisplay);

  Attribution sourceId(String? sourceId);

  Attribution termsUrl(String? termsUrl);

  Attribution count(int? count);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Attribution(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Attribution(...).copyWith(id: 12, name: "My name")
  /// ```
  Attribution call({
    String text,
    String? html,
    String? url,
    String licence,
    String? licenceUrl,
    bool requiredDisplay,
    String? sourceId,
    String? termsUrl,
    int? count,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAttribution.copyWith(...)` or call `instanceOfAttribution.copyWith.fieldName(value)` for a single field.
class _$AttributionCWProxyImpl implements _$AttributionCWProxy {
  const _$AttributionCWProxyImpl(this._value);

  final Attribution _value;

  @override
  Attribution text(String text) => call(text: text);

  @override
  Attribution html(String? html) => call(html: html);

  @override
  Attribution url(String? url) => call(url: url);

  @override
  Attribution licence(String licence) => call(licence: licence);

  @override
  Attribution licenceUrl(String? licenceUrl) => call(licenceUrl: licenceUrl);

  @override
  Attribution requiredDisplay(bool requiredDisplay) =>
      call(requiredDisplay: requiredDisplay);

  @override
  Attribution sourceId(String? sourceId) => call(sourceId: sourceId);

  @override
  Attribution termsUrl(String? termsUrl) => call(termsUrl: termsUrl);

  @override
  Attribution count(int? count) => call(count: count);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Attribution(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Attribution(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Attribution call({
    Object? text = const $CopyWithPlaceholder(),
    Object? html = const $CopyWithPlaceholder(),
    Object? url = const $CopyWithPlaceholder(),
    Object? licence = const $CopyWithPlaceholder(),
    Object? licenceUrl = const $CopyWithPlaceholder(),
    Object? requiredDisplay = const $CopyWithPlaceholder(),
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? termsUrl = const $CopyWithPlaceholder(),
    Object? count = const $CopyWithPlaceholder(),
  }) {
    return Attribution(
      text: text == const $CopyWithPlaceholder() || text == null
          ? _value.text
          // ignore: cast_nullable_to_non_nullable
          : text as String,
      html: html == const $CopyWithPlaceholder()
          ? _value.html
          // ignore: cast_nullable_to_non_nullable
          : html as String?,
      url: url == const $CopyWithPlaceholder()
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String?,
      licence: licence == const $CopyWithPlaceholder() || licence == null
          ? _value.licence
          // ignore: cast_nullable_to_non_nullable
          : licence as String,
      licenceUrl: licenceUrl == const $CopyWithPlaceholder()
          ? _value.licenceUrl
          // ignore: cast_nullable_to_non_nullable
          : licenceUrl as String?,
      requiredDisplay:
          requiredDisplay == const $CopyWithPlaceholder() ||
              requiredDisplay == null
          ? _value.requiredDisplay
          // ignore: cast_nullable_to_non_nullable
          : requiredDisplay as bool,
      sourceId: sourceId == const $CopyWithPlaceholder()
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String?,
      termsUrl: termsUrl == const $CopyWithPlaceholder()
          ? _value.termsUrl
          // ignore: cast_nullable_to_non_nullable
          : termsUrl as String?,
      count: count == const $CopyWithPlaceholder()
          ? _value.count
          // ignore: cast_nullable_to_non_nullable
          : count as int?,
    );
  }
}

extension $AttributionCopyWith on Attribution {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAttribution.copyWith(...)` or `instanceOfAttribution.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AttributionCWProxy get copyWith => _$AttributionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Attribution _$AttributionFromJson(Map<String, dynamic> json) =>
    $checkedCreate('Attribution', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['text', 'licence', 'requiredDisplay'],
      );
      final val = Attribution(
        text: $checkedConvert('text', (v) => v as String),
        html: $checkedConvert('html', (v) => v as String?),
        url: $checkedConvert('url', (v) => v as String?),
        licence: $checkedConvert('licence', (v) => v as String),
        licenceUrl: $checkedConvert('licenceUrl', (v) => v as String?),
        requiredDisplay: $checkedConvert('requiredDisplay', (v) => v as bool),
        sourceId: $checkedConvert('sourceId', (v) => v as String?),
        termsUrl: $checkedConvert('termsUrl', (v) => v as String?),
        count: $checkedConvert('count', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$AttributionToJson(Attribution instance) =>
    <String, dynamic>{
      'text': instance.text,
      'html': ?instance.html,
      'url': ?instance.url,
      'licence': instance.licence,
      'licenceUrl': ?instance.licenceUrl,
      'requiredDisplay': instance.requiredDisplay,
      'sourceId': ?instance.sourceId,
      'termsUrl': ?instance.termsUrl,
      'count': ?instance.count,
    };
