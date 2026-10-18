// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'glance_briefing.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$GlanceBriefingCWProxy {
  GlanceBriefing title(String? title);

  GlanceBriefing at(DateTime? at);

  GlanceBriefing url(String? url);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GlanceBriefing(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GlanceBriefing(...).copyWith(id: 12, name: "My name")
  /// ```
  GlanceBriefing call({String? title, DateTime? at, String? url});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfGlanceBriefing.copyWith(...)` or call `instanceOfGlanceBriefing.copyWith.fieldName(value)` for a single field.
class _$GlanceBriefingCWProxyImpl implements _$GlanceBriefingCWProxy {
  const _$GlanceBriefingCWProxyImpl(this._value);

  final GlanceBriefing _value;

  @override
  GlanceBriefing title(String? title) => call(title: title);

  @override
  GlanceBriefing at(DateTime? at) => call(at: at);

  @override
  GlanceBriefing url(String? url) => call(url: url);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GlanceBriefing(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GlanceBriefing(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  GlanceBriefing call({
    Object? title = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
    Object? url = const $CopyWithPlaceholder(),
  }) {
    return GlanceBriefing(
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      at: at == const $CopyWithPlaceholder()
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime?,
      url: url == const $CopyWithPlaceholder()
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String?,
    );
  }
}

extension $GlanceBriefingCopyWith on GlanceBriefing {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfGlanceBriefing.copyWith(...)` or `instanceOfGlanceBriefing.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$GlanceBriefingCWProxy get copyWith => _$GlanceBriefingCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

GlanceBriefing _$GlanceBriefingFromJson(Map<String, dynamic> json) =>
    $checkedCreate('GlanceBriefing', json, ($checkedConvert) {
      final val = GlanceBriefing(
        title: $checkedConvert('title', (v) => v as String?),
        at: $checkedConvert(
          'at',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        url: $checkedConvert('url', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$GlanceBriefingToJson(GlanceBriefing instance) =>
    <String, dynamic>{
      'title': ?instance.title,
      'at': ?instance.at?.toIso8601String(),
      'url': ?instance.url,
    };
