// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'outlook_published_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OutlookPublishedEventCWProxy {
  OutlookPublishedEvent region(String region);

  OutlookPublishedEvent family(String family);

  OutlookPublishedEvent publishedAt(DateTime publishedAt);

  OutlookPublishedEvent windows(List<OutlookWindow>? windows);

  OutlookPublishedEvent recordRef(String? recordRef);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OutlookPublishedEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OutlookPublishedEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  OutlookPublishedEvent call({
    String region,
    String family,
    DateTime publishedAt,
    List<OutlookWindow>? windows,
    String? recordRef,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOutlookPublishedEvent.copyWith(...)` or call `instanceOfOutlookPublishedEvent.copyWith.fieldName(value)` for a single field.
class _$OutlookPublishedEventCWProxyImpl
    implements _$OutlookPublishedEventCWProxy {
  const _$OutlookPublishedEventCWProxyImpl(this._value);

  final OutlookPublishedEvent _value;

  @override
  OutlookPublishedEvent region(String region) => call(region: region);

  @override
  OutlookPublishedEvent family(String family) => call(family: family);

  @override
  OutlookPublishedEvent publishedAt(DateTime publishedAt) =>
      call(publishedAt: publishedAt);

  @override
  OutlookPublishedEvent windows(List<OutlookWindow>? windows) =>
      call(windows: windows);

  @override
  OutlookPublishedEvent recordRef(String? recordRef) =>
      call(recordRef: recordRef);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OutlookPublishedEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OutlookPublishedEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OutlookPublishedEvent call({
    Object? region = const $CopyWithPlaceholder(),
    Object? family = const $CopyWithPlaceholder(),
    Object? publishedAt = const $CopyWithPlaceholder(),
    Object? windows = const $CopyWithPlaceholder(),
    Object? recordRef = const $CopyWithPlaceholder(),
  }) {
    return OutlookPublishedEvent(
      region: region == const $CopyWithPlaceholder() || region == null
          ? _value.region
          // ignore: cast_nullable_to_non_nullable
          : region as String,
      family: family == const $CopyWithPlaceholder() || family == null
          ? _value.family
          // ignore: cast_nullable_to_non_nullable
          : family as String,
      publishedAt:
          publishedAt == const $CopyWithPlaceholder() || publishedAt == null
          ? _value.publishedAt
          // ignore: cast_nullable_to_non_nullable
          : publishedAt as DateTime,
      windows: windows == const $CopyWithPlaceholder()
          ? _value.windows
          // ignore: cast_nullable_to_non_nullable
          : windows as List<OutlookWindow>?,
      recordRef: recordRef == const $CopyWithPlaceholder()
          ? _value.recordRef
          // ignore: cast_nullable_to_non_nullable
          : recordRef as String?,
    );
  }
}

extension $OutlookPublishedEventCopyWith on OutlookPublishedEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOutlookPublishedEvent.copyWith(...)` or `instanceOfOutlookPublishedEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OutlookPublishedEventCWProxy get copyWith =>
      _$OutlookPublishedEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OutlookPublishedEvent _$OutlookPublishedEventFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('OutlookPublishedEvent', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['region', 'family', 'publishedAt']);
  final val = OutlookPublishedEvent(
    region: $checkedConvert('region', (v) => v as String),
    family: $checkedConvert('family', (v) => v as String),
    publishedAt: $checkedConvert(
      'publishedAt',
      (v) => DateTime.parse(v as String),
    ),
    windows: $checkedConvert(
      'windows',
      (v) => (v as List<dynamic>?)
          ?.map((e) => OutlookWindow.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    recordRef: $checkedConvert('recordRef', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$OutlookPublishedEventToJson(
  OutlookPublishedEvent instance,
) => <String, dynamic>{
  'region': instance.region,
  'family': instance.family,
  'publishedAt': instance.publishedAt.toIso8601String(),
  'windows': ?instance.windows?.map((e) => e.toJson()).toList(),
  'recordRef': ?instance.recordRef,
};
