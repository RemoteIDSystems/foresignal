// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'report_block_one_of10_all_of_attributions_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReportBlockOneOf10AllOfAttributionsInnerCWProxy {
  ReportBlockOneOf10AllOfAttributionsInner sourceId(String sourceId);

  ReportBlockOneOf10AllOfAttributionsInner licence(String? licence);

  ReportBlockOneOf10AllOfAttributionsInner attribution(String attribution);

  ReportBlockOneOf10AllOfAttributionsInner url(String? url);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlockOneOf10AllOfAttributionsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlockOneOf10AllOfAttributionsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ReportBlockOneOf10AllOfAttributionsInner call({
    String sourceId,
    String? licence,
    String attribution,
    String? url,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReportBlockOneOf10AllOfAttributionsInner.copyWith(...)` or call `instanceOfReportBlockOneOf10AllOfAttributionsInner.copyWith.fieldName(value)` for a single field.
class _$ReportBlockOneOf10AllOfAttributionsInnerCWProxyImpl
    implements _$ReportBlockOneOf10AllOfAttributionsInnerCWProxy {
  const _$ReportBlockOneOf10AllOfAttributionsInnerCWProxyImpl(this._value);

  final ReportBlockOneOf10AllOfAttributionsInner _value;

  @override
  ReportBlockOneOf10AllOfAttributionsInner sourceId(String sourceId) =>
      call(sourceId: sourceId);

  @override
  ReportBlockOneOf10AllOfAttributionsInner licence(String? licence) =>
      call(licence: licence);

  @override
  ReportBlockOneOf10AllOfAttributionsInner attribution(String attribution) =>
      call(attribution: attribution);

  @override
  ReportBlockOneOf10AllOfAttributionsInner url(String? url) => call(url: url);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlockOneOf10AllOfAttributionsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlockOneOf10AllOfAttributionsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReportBlockOneOf10AllOfAttributionsInner call({
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? licence = const $CopyWithPlaceholder(),
    Object? attribution = const $CopyWithPlaceholder(),
    Object? url = const $CopyWithPlaceholder(),
  }) {
    return ReportBlockOneOf10AllOfAttributionsInner(
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      licence: licence == const $CopyWithPlaceholder()
          ? _value.licence
          // ignore: cast_nullable_to_non_nullable
          : licence as String?,
      attribution:
          attribution == const $CopyWithPlaceholder() || attribution == null
          ? _value.attribution
          // ignore: cast_nullable_to_non_nullable
          : attribution as String,
      url: url == const $CopyWithPlaceholder()
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String?,
    );
  }
}

extension $ReportBlockOneOf10AllOfAttributionsInnerCopyWith
    on ReportBlockOneOf10AllOfAttributionsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReportBlockOneOf10AllOfAttributionsInner.copyWith(...)` or `instanceOfReportBlockOneOf10AllOfAttributionsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReportBlockOneOf10AllOfAttributionsInnerCWProxy get copyWith =>
      _$ReportBlockOneOf10AllOfAttributionsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReportBlockOneOf10AllOfAttributionsInner
_$ReportBlockOneOf10AllOfAttributionsInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ReportBlockOneOf10AllOfAttributionsInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['sourceId', 'attribution']);
      final val = ReportBlockOneOf10AllOfAttributionsInner(
        sourceId: $checkedConvert('sourceId', (v) => v as String),
        licence: $checkedConvert('licence', (v) => v as String?),
        attribution: $checkedConvert('attribution', (v) => v as String),
        url: $checkedConvert('url', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ReportBlockOneOf10AllOfAttributionsInnerToJson(
  ReportBlockOneOf10AllOfAttributionsInner instance,
) => <String, dynamic>{
  'sourceId': instance.sourceId,
  'licence': ?instance.licence,
  'attribution': instance.attribution,
  'url': ?instance.url,
};
