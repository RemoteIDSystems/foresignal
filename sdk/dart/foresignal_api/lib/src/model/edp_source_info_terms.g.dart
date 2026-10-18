// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_source_info_terms.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpSourceInfoTermsCWProxy {
  EdpSourceInfoTerms summary(String? summary);

  EdpSourceInfoTerms url(String? url);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpSourceInfoTerms(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpSourceInfoTerms(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpSourceInfoTerms call({String? summary, String? url});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpSourceInfoTerms.copyWith(...)` or call `instanceOfEdpSourceInfoTerms.copyWith.fieldName(value)` for a single field.
class _$EdpSourceInfoTermsCWProxyImpl implements _$EdpSourceInfoTermsCWProxy {
  const _$EdpSourceInfoTermsCWProxyImpl(this._value);

  final EdpSourceInfoTerms _value;

  @override
  EdpSourceInfoTerms summary(String? summary) => call(summary: summary);

  @override
  EdpSourceInfoTerms url(String? url) => call(url: url);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpSourceInfoTerms(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpSourceInfoTerms(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpSourceInfoTerms call({
    Object? summary = const $CopyWithPlaceholder(),
    Object? url = const $CopyWithPlaceholder(),
  }) {
    return EdpSourceInfoTerms(
      summary: summary == const $CopyWithPlaceholder()
          ? _value.summary
          // ignore: cast_nullable_to_non_nullable
          : summary as String?,
      url: url == const $CopyWithPlaceholder()
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String?,
    );
  }
}

extension $EdpSourceInfoTermsCopyWith on EdpSourceInfoTerms {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpSourceInfoTerms.copyWith(...)` or `instanceOfEdpSourceInfoTerms.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpSourceInfoTermsCWProxy get copyWith =>
      _$EdpSourceInfoTermsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpSourceInfoTerms _$EdpSourceInfoTermsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EdpSourceInfoTerms', json, ($checkedConvert) {
      final val = EdpSourceInfoTerms(
        summary: $checkedConvert('summary', (v) => v as String?),
        url: $checkedConvert('url', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$EdpSourceInfoTermsToJson(EdpSourceInfoTerms instance) =>
    <String, dynamic>{'summary': ?instance.summary, 'url': ?instance.url};
