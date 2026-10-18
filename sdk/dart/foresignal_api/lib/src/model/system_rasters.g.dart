// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'system_rasters.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SystemRastersCWProxy {
  SystemRasters profile(SystemRastersProfileEnum profile);

  SystemRasters products(int? products);

  SystemRasters frames(int? frames);

  SystemRasters storeBytes(int? storeBytes);

  SystemRasters mediaBytes(int? mediaBytes);

  SystemRasters diskQuotaGb(num? diskQuotaGb);

  SystemRasters diskQuotaPct(num? diskQuotaPct);

  SystemRasters caches(Map<String, SystemRastersCachesValue>? caches);

  SystemRasters renderQueue(SystemRastersRenderQueue? renderQueue);

  SystemRasters renderBusyLastHour(int? renderBusyLastHour);

  SystemRasters mbDownloadedToday(num? mbDownloadedToday);

  SystemRasters glm(SystemRastersGlm? glm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemRasters(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemRasters(...).copyWith(id: 12, name: "My name")
  /// ```
  SystemRasters call({
    SystemRastersProfileEnum profile,
    int? products,
    int? frames,
    int? storeBytes,
    int? mediaBytes,
    num? diskQuotaGb,
    num? diskQuotaPct,
    Map<String, SystemRastersCachesValue>? caches,
    SystemRastersRenderQueue? renderQueue,
    int? renderBusyLastHour,
    num? mbDownloadedToday,
    SystemRastersGlm? glm,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSystemRasters.copyWith(...)` or call `instanceOfSystemRasters.copyWith.fieldName(value)` for a single field.
class _$SystemRastersCWProxyImpl implements _$SystemRastersCWProxy {
  const _$SystemRastersCWProxyImpl(this._value);

  final SystemRasters _value;

  @override
  SystemRasters profile(SystemRastersProfileEnum profile) =>
      call(profile: profile);

  @override
  SystemRasters products(int? products) => call(products: products);

  @override
  SystemRasters frames(int? frames) => call(frames: frames);

  @override
  SystemRasters storeBytes(int? storeBytes) => call(storeBytes: storeBytes);

  @override
  SystemRasters mediaBytes(int? mediaBytes) => call(mediaBytes: mediaBytes);

  @override
  SystemRasters diskQuotaGb(num? diskQuotaGb) => call(diskQuotaGb: diskQuotaGb);

  @override
  SystemRasters diskQuotaPct(num? diskQuotaPct) =>
      call(diskQuotaPct: diskQuotaPct);

  @override
  SystemRasters caches(Map<String, SystemRastersCachesValue>? caches) =>
      call(caches: caches);

  @override
  SystemRasters renderQueue(SystemRastersRenderQueue? renderQueue) =>
      call(renderQueue: renderQueue);

  @override
  SystemRasters renderBusyLastHour(int? renderBusyLastHour) =>
      call(renderBusyLastHour: renderBusyLastHour);

  @override
  SystemRasters mbDownloadedToday(num? mbDownloadedToday) =>
      call(mbDownloadedToday: mbDownloadedToday);

  @override
  SystemRasters glm(SystemRastersGlm? glm) => call(glm: glm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemRasters(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemRasters(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SystemRasters call({
    Object? profile = const $CopyWithPlaceholder(),
    Object? products = const $CopyWithPlaceholder(),
    Object? frames = const $CopyWithPlaceholder(),
    Object? storeBytes = const $CopyWithPlaceholder(),
    Object? mediaBytes = const $CopyWithPlaceholder(),
    Object? diskQuotaGb = const $CopyWithPlaceholder(),
    Object? diskQuotaPct = const $CopyWithPlaceholder(),
    Object? caches = const $CopyWithPlaceholder(),
    Object? renderQueue = const $CopyWithPlaceholder(),
    Object? renderBusyLastHour = const $CopyWithPlaceholder(),
    Object? mbDownloadedToday = const $CopyWithPlaceholder(),
    Object? glm = const $CopyWithPlaceholder(),
  }) {
    return SystemRasters(
      profile: profile == const $CopyWithPlaceholder() || profile == null
          ? _value.profile
          // ignore: cast_nullable_to_non_nullable
          : profile as SystemRastersProfileEnum,
      products: products == const $CopyWithPlaceholder()
          ? _value.products
          // ignore: cast_nullable_to_non_nullable
          : products as int?,
      frames: frames == const $CopyWithPlaceholder()
          ? _value.frames
          // ignore: cast_nullable_to_non_nullable
          : frames as int?,
      storeBytes: storeBytes == const $CopyWithPlaceholder()
          ? _value.storeBytes
          // ignore: cast_nullable_to_non_nullable
          : storeBytes as int?,
      mediaBytes: mediaBytes == const $CopyWithPlaceholder()
          ? _value.mediaBytes
          // ignore: cast_nullable_to_non_nullable
          : mediaBytes as int?,
      diskQuotaGb: diskQuotaGb == const $CopyWithPlaceholder()
          ? _value.diskQuotaGb
          // ignore: cast_nullable_to_non_nullable
          : diskQuotaGb as num?,
      diskQuotaPct: diskQuotaPct == const $CopyWithPlaceholder()
          ? _value.diskQuotaPct
          // ignore: cast_nullable_to_non_nullable
          : diskQuotaPct as num?,
      caches: caches == const $CopyWithPlaceholder()
          ? _value.caches
          // ignore: cast_nullable_to_non_nullable
          : caches as Map<String, SystemRastersCachesValue>?,
      renderQueue: renderQueue == const $CopyWithPlaceholder()
          ? _value.renderQueue
          // ignore: cast_nullable_to_non_nullable
          : renderQueue as SystemRastersRenderQueue?,
      renderBusyLastHour: renderBusyLastHour == const $CopyWithPlaceholder()
          ? _value.renderBusyLastHour
          // ignore: cast_nullable_to_non_nullable
          : renderBusyLastHour as int?,
      mbDownloadedToday: mbDownloadedToday == const $CopyWithPlaceholder()
          ? _value.mbDownloadedToday
          // ignore: cast_nullable_to_non_nullable
          : mbDownloadedToday as num?,
      glm: glm == const $CopyWithPlaceholder()
          ? _value.glm
          // ignore: cast_nullable_to_non_nullable
          : glm as SystemRastersGlm?,
    );
  }
}

extension $SystemRastersCopyWith on SystemRasters {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSystemRasters.copyWith(...)` or `instanceOfSystemRasters.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SystemRastersCWProxy get copyWith => _$SystemRastersCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SystemRasters _$SystemRastersFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SystemRasters', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['profile']);
      final val = SystemRasters(
        profile: $checkedConvert(
          'profile',
          (v) => $enumDecode(_$SystemRastersProfileEnumEnumMap, v),
        ),
        products: $checkedConvert('products', (v) => (v as num?)?.toInt()),
        frames: $checkedConvert('frames', (v) => (v as num?)?.toInt()),
        storeBytes: $checkedConvert('storeBytes', (v) => (v as num?)?.toInt()),
        mediaBytes: $checkedConvert('mediaBytes', (v) => (v as num?)?.toInt()),
        diskQuotaGb: $checkedConvert('diskQuotaGb', (v) => v as num?),
        diskQuotaPct: $checkedConvert('diskQuotaPct', (v) => v as num?),
        caches: $checkedConvert(
          'caches',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(
              k,
              SystemRastersCachesValue.fromJson(e as Map<String, dynamic>),
            ),
          ),
        ),
        renderQueue: $checkedConvert(
          'renderQueue',
          (v) => v == null
              ? null
              : SystemRastersRenderQueue.fromJson(v as Map<String, dynamic>),
        ),
        renderBusyLastHour: $checkedConvert(
          'renderBusyLastHour',
          (v) => (v as num?)?.toInt(),
        ),
        mbDownloadedToday: $checkedConvert(
          'mbDownloadedToday',
          (v) => v as num?,
        ),
        glm: $checkedConvert(
          'glm',
          (v) => v == null
              ? null
              : SystemRastersGlm.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SystemRastersToJson(SystemRasters instance) =>
    <String, dynamic>{
      'profile': _$SystemRastersProfileEnumEnumMap[instance.profile]!,
      'products': ?instance.products,
      'frames': ?instance.frames,
      'storeBytes': ?instance.storeBytes,
      'mediaBytes': ?instance.mediaBytes,
      'diskQuotaGb': ?instance.diskQuotaGb,
      'diskQuotaPct': ?instance.diskQuotaPct,
      'caches': ?instance.caches?.map((k, e) => MapEntry(k, e.toJson())),
      'renderQueue': ?instance.renderQueue?.toJson(),
      'renderBusyLastHour': ?instance.renderBusyLastHour,
      'mbDownloadedToday': ?instance.mbDownloadedToday,
      'glm': ?instance.glm?.toJson(),
    };

const _$SystemRastersProfileEnumEnumMap = {
  SystemRastersProfileEnum.small: 'small',
  SystemRastersProfileEnum.large: 'large',
};
