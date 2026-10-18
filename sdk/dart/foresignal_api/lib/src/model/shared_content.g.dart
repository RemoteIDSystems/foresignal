// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'shared_content.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SharedContentCWProxy {
  SharedContent targetType(SharedContentTargetTypeEnum targetType);

  SharedContent title(String? title);

  SharedContent expiresAt(DateTime expiresAt);

  SharedContent masked(bool masked);

  SharedContent precisionM(int? precisionM);

  SharedContent data(Map<String, Object> data);

  SharedContent attributions(List<Attribution> attributions);

  SharedContent links(SharedContentLinks? links);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SharedContent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SharedContent(...).copyWith(id: 12, name: "My name")
  /// ```
  SharedContent call({
    SharedContentTargetTypeEnum targetType,
    String? title,
    DateTime expiresAt,
    bool masked,
    int? precisionM,
    Map<String, Object> data,
    List<Attribution> attributions,
    SharedContentLinks? links,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSharedContent.copyWith(...)` or call `instanceOfSharedContent.copyWith.fieldName(value)` for a single field.
class _$SharedContentCWProxyImpl implements _$SharedContentCWProxy {
  const _$SharedContentCWProxyImpl(this._value);

  final SharedContent _value;

  @override
  SharedContent targetType(SharedContentTargetTypeEnum targetType) =>
      call(targetType: targetType);

  @override
  SharedContent title(String? title) => call(title: title);

  @override
  SharedContent expiresAt(DateTime expiresAt) => call(expiresAt: expiresAt);

  @override
  SharedContent masked(bool masked) => call(masked: masked);

  @override
  SharedContent precisionM(int? precisionM) => call(precisionM: precisionM);

  @override
  SharedContent data(Map<String, Object> data) => call(data: data);

  @override
  SharedContent attributions(List<Attribution> attributions) =>
      call(attributions: attributions);

  @override
  SharedContent links(SharedContentLinks? links) => call(links: links);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SharedContent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SharedContent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SharedContent call({
    Object? targetType = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
    Object? masked = const $CopyWithPlaceholder(),
    Object? precisionM = const $CopyWithPlaceholder(),
    Object? data = const $CopyWithPlaceholder(),
    Object? attributions = const $CopyWithPlaceholder(),
    Object? links = const $CopyWithPlaceholder(),
  }) {
    return SharedContent(
      targetType:
          targetType == const $CopyWithPlaceholder() || targetType == null
          ? _value.targetType
          // ignore: cast_nullable_to_non_nullable
          : targetType as SharedContentTargetTypeEnum,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      expiresAt: expiresAt == const $CopyWithPlaceholder() || expiresAt == null
          ? _value.expiresAt
          // ignore: cast_nullable_to_non_nullable
          : expiresAt as DateTime,
      masked: masked == const $CopyWithPlaceholder() || masked == null
          ? _value.masked
          // ignore: cast_nullable_to_non_nullable
          : masked as bool,
      precisionM: precisionM == const $CopyWithPlaceholder()
          ? _value.precisionM
          // ignore: cast_nullable_to_non_nullable
          : precisionM as int?,
      data: data == const $CopyWithPlaceholder() || data == null
          ? _value.data
          // ignore: cast_nullable_to_non_nullable
          : data as Map<String, Object>,
      attributions:
          attributions == const $CopyWithPlaceholder() || attributions == null
          ? _value.attributions
          // ignore: cast_nullable_to_non_nullable
          : attributions as List<Attribution>,
      links: links == const $CopyWithPlaceholder()
          ? _value.links
          // ignore: cast_nullable_to_non_nullable
          : links as SharedContentLinks?,
    );
  }
}

extension $SharedContentCopyWith on SharedContent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSharedContent.copyWith(...)` or `instanceOfSharedContent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SharedContentCWProxy get copyWith => _$SharedContentCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SharedContent _$SharedContentFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SharedContent', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'targetType',
      'expiresAt',
      'masked',
      'data',
      'attributions',
    ],
  );
  final val = SharedContent(
    targetType: $checkedConvert(
      'targetType',
      (v) => $enumDecode(_$SharedContentTargetTypeEnumEnumMap, v),
    ),
    title: $checkedConvert('title', (v) => v as String?),
    expiresAt: $checkedConvert('expiresAt', (v) => DateTime.parse(v as String)),
    masked: $checkedConvert('masked', (v) => v as bool),
    precisionM: $checkedConvert('precisionM', (v) => (v as num?)?.toInt()),
    data: $checkedConvert(
      'data',
      (v) =>
          (v as Map<String, dynamic>).map((k, e) => MapEntry(k, e as Object)),
    ),
    attributions: $checkedConvert(
      'attributions',
      (v) => (v as List<dynamic>)
          .map((e) => Attribution.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    links: $checkedConvert(
      'links',
      (v) => v == null
          ? null
          : SharedContentLinks.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$SharedContentToJson(SharedContent instance) =>
    <String, dynamic>{
      'targetType': _$SharedContentTargetTypeEnumEnumMap[instance.targetType]!,
      'title': ?instance.title,
      'expiresAt': instance.expiresAt.toIso8601String(),
      'masked': instance.masked,
      'precisionM': ?instance.precisionM,
      'data': instance.data,
      'attributions': instance.attributions.map((e) => e.toJson()).toList(),
      'links': ?instance.links?.toJson(),
    };

const _$SharedContentTargetTypeEnumEnumMap = {
  SharedContentTargetTypeEnum.record: 'record',
  SharedContentTargetTypeEnum.alert: 'alert',
  SharedContentTargetTypeEnum.view: 'view',
  SharedContentTargetTypeEnum.track: 'track',
  SharedContentTargetTypeEnum.briefingRun: 'briefing-run',
  SharedContentTargetTypeEnum.export_: 'export',
};
