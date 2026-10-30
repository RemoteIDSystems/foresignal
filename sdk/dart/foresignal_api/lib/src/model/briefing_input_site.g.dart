// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'briefing_input_site.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BriefingInputSiteCWProxy {
  BriefingInputSite enabled(bool? enabled);

  BriefingInputSite shareable(bool? shareable);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BriefingInputSite(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BriefingInputSite(...).copyWith(id: 12, name: "My name")
  /// ```
  BriefingInputSite call({bool? enabled, bool? shareable});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBriefingInputSite.copyWith(...)` or call `instanceOfBriefingInputSite.copyWith.fieldName(value)` for a single field.
class _$BriefingInputSiteCWProxyImpl implements _$BriefingInputSiteCWProxy {
  const _$BriefingInputSiteCWProxyImpl(this._value);

  final BriefingInputSite _value;

  @override
  BriefingInputSite enabled(bool? enabled) => call(enabled: enabled);

  @override
  BriefingInputSite shareable(bool? shareable) => call(shareable: shareable);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BriefingInputSite(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BriefingInputSite(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BriefingInputSite call({
    Object? enabled = const $CopyWithPlaceholder(),
    Object? shareable = const $CopyWithPlaceholder(),
  }) {
    return BriefingInputSite(
      enabled: enabled == const $CopyWithPlaceholder()
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool?,
      shareable: shareable == const $CopyWithPlaceholder()
          ? _value.shareable
          // ignore: cast_nullable_to_non_nullable
          : shareable as bool?,
    );
  }
}

extension $BriefingInputSiteCopyWith on BriefingInputSite {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBriefingInputSite.copyWith(...)` or `instanceOfBriefingInputSite.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BriefingInputSiteCWProxy get copyWith =>
      _$BriefingInputSiteCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BriefingInputSite _$BriefingInputSiteFromJson(Map<String, dynamic> json) =>
    $checkedCreate('BriefingInputSite', json, ($checkedConvert) {
      final val = BriefingInputSite(
        enabled: $checkedConvert('enabled', (v) => v as bool? ?? false),
        shareable: $checkedConvert('shareable', (v) => v as bool? ?? false),
      );
      return val;
    });

Map<String, dynamic> _$BriefingInputSiteToJson(BriefingInputSite instance) =>
    <String, dynamic>{
      'enabled': ?instance.enabled,
      'shareable': ?instance.shareable,
    };
