// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'raster_frame_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RasterFramePageCWProxy {
  RasterFramePage items(List<RasterFrame> items);

  RasterFramePage nextCursor(String? nextCursor);

  RasterFramePage prefetch(RasterFramePagePrefetch? prefetch);

  RasterFramePage serverTime(DateTime serverTime);

  RasterFramePage earliest(DateTime? earliest);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RasterFramePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RasterFramePage(...).copyWith(id: 12, name: "My name")
  /// ```
  RasterFramePage call({
    List<RasterFrame> items,
    String? nextCursor,
    RasterFramePagePrefetch? prefetch,
    DateTime serverTime,
    DateTime? earliest,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRasterFramePage.copyWith(...)` or call `instanceOfRasterFramePage.copyWith.fieldName(value)` for a single field.
class _$RasterFramePageCWProxyImpl implements _$RasterFramePageCWProxy {
  const _$RasterFramePageCWProxyImpl(this._value);

  final RasterFramePage _value;

  @override
  RasterFramePage items(List<RasterFrame> items) => call(items: items);

  @override
  RasterFramePage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  @override
  RasterFramePage prefetch(RasterFramePagePrefetch? prefetch) =>
      call(prefetch: prefetch);

  @override
  RasterFramePage serverTime(DateTime serverTime) =>
      call(serverTime: serverTime);

  @override
  RasterFramePage earliest(DateTime? earliest) => call(earliest: earliest);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RasterFramePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RasterFramePage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RasterFramePage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
    Object? prefetch = const $CopyWithPlaceholder(),
    Object? serverTime = const $CopyWithPlaceholder(),
    Object? earliest = const $CopyWithPlaceholder(),
  }) {
    return RasterFramePage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<RasterFrame>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
      prefetch: prefetch == const $CopyWithPlaceholder()
          ? _value.prefetch
          // ignore: cast_nullable_to_non_nullable
          : prefetch as RasterFramePagePrefetch?,
      serverTime:
          serverTime == const $CopyWithPlaceholder() || serverTime == null
          ? _value.serverTime
          // ignore: cast_nullable_to_non_nullable
          : serverTime as DateTime,
      earliest: earliest == const $CopyWithPlaceholder()
          ? _value.earliest
          // ignore: cast_nullable_to_non_nullable
          : earliest as DateTime?,
    );
  }
}

extension $RasterFramePageCopyWith on RasterFramePage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRasterFramePage.copyWith(...)` or `instanceOfRasterFramePage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RasterFramePageCWProxy get copyWith => _$RasterFramePageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RasterFramePage _$RasterFramePageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('RasterFramePage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items', 'serverTime']);
      final val = RasterFramePage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => RasterFrame.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
        prefetch: $checkedConvert(
          'prefetch',
          (v) => v == null
              ? null
              : RasterFramePagePrefetch.fromJson(v as Map<String, dynamic>),
        ),
        serverTime: $checkedConvert(
          'serverTime',
          (v) => DateTime.parse(v as String),
        ),
        earliest: $checkedConvert(
          'earliest',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$RasterFramePageToJson(RasterFramePage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
      'prefetch': ?instance.prefetch?.toJson(),
      'serverTime': instance.serverTime.toIso8601String(),
      'earliest': ?instance.earliest?.toIso8601String(),
    };
