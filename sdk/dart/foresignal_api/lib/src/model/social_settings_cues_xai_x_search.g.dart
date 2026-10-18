// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_settings_cues_xai_x_search.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialSettingsCuesXaiXSearchCWProxy {
  SocialSettingsCuesXaiXSearch enabled(bool? enabled);

  SocialSettingsCuesXaiXSearch maxResults(int? maxResults);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialSettingsCuesXaiXSearch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialSettingsCuesXaiXSearch(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialSettingsCuesXaiXSearch call({bool? enabled, int? maxResults});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialSettingsCuesXaiXSearch.copyWith(...)` or call `instanceOfSocialSettingsCuesXaiXSearch.copyWith.fieldName(value)` for a single field.
class _$SocialSettingsCuesXaiXSearchCWProxyImpl
    implements _$SocialSettingsCuesXaiXSearchCWProxy {
  const _$SocialSettingsCuesXaiXSearchCWProxyImpl(this._value);

  final SocialSettingsCuesXaiXSearch _value;

  @override
  SocialSettingsCuesXaiXSearch enabled(bool? enabled) => call(enabled: enabled);

  @override
  SocialSettingsCuesXaiXSearch maxResults(int? maxResults) =>
      call(maxResults: maxResults);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialSettingsCuesXaiXSearch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialSettingsCuesXaiXSearch(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialSettingsCuesXaiXSearch call({
    Object? enabled = const $CopyWithPlaceholder(),
    Object? maxResults = const $CopyWithPlaceholder(),
  }) {
    return SocialSettingsCuesXaiXSearch(
      enabled: enabled == const $CopyWithPlaceholder()
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool?,
      maxResults: maxResults == const $CopyWithPlaceholder()
          ? _value.maxResults
          // ignore: cast_nullable_to_non_nullable
          : maxResults as int?,
    );
  }
}

extension $SocialSettingsCuesXaiXSearchCopyWith
    on SocialSettingsCuesXaiXSearch {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialSettingsCuesXaiXSearch.copyWith(...)` or `instanceOfSocialSettingsCuesXaiXSearch.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialSettingsCuesXaiXSearchCWProxy get copyWith =>
      _$SocialSettingsCuesXaiXSearchCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialSettingsCuesXaiXSearch _$SocialSettingsCuesXaiXSearchFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SocialSettingsCuesXaiXSearch', json, ($checkedConvert) {
  final val = SocialSettingsCuesXaiXSearch(
    enabled: $checkedConvert('enabled', (v) => v as bool? ?? false),
    maxResults: $checkedConvert(
      'maxResults',
      (v) => (v as num?)?.toInt() ?? 20,
    ),
  );
  return val;
});

Map<String, dynamic> _$SocialSettingsCuesXaiXSearchToJson(
  SocialSettingsCuesXaiXSearch instance,
) => <String, dynamic>{
  'enabled': ?instance.enabled,
  'maxResults': ?instance.maxResults,
};
