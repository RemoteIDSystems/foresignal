// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'source_detail.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SourceDetailCWProxy {
  SourceDetail descriptor(SourceDescriptor descriptor);

  SourceDetail settings(SourceSettings settings);

  SourceDetail health(SourceHealth health);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SourceDetail(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SourceDetail(...).copyWith(id: 12, name: "My name")
  /// ```
  SourceDetail call({
    SourceDescriptor descriptor,
    SourceSettings settings,
    SourceHealth health,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSourceDetail.copyWith(...)` or call `instanceOfSourceDetail.copyWith.fieldName(value)` for a single field.
class _$SourceDetailCWProxyImpl implements _$SourceDetailCWProxy {
  const _$SourceDetailCWProxyImpl(this._value);

  final SourceDetail _value;

  @override
  SourceDetail descriptor(SourceDescriptor descriptor) =>
      call(descriptor: descriptor);

  @override
  SourceDetail settings(SourceSettings settings) => call(settings: settings);

  @override
  SourceDetail health(SourceHealth health) => call(health: health);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SourceDetail(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SourceDetail(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SourceDetail call({
    Object? descriptor = const $CopyWithPlaceholder(),
    Object? settings = const $CopyWithPlaceholder(),
    Object? health = const $CopyWithPlaceholder(),
  }) {
    return SourceDetail(
      descriptor:
          descriptor == const $CopyWithPlaceholder() || descriptor == null
          ? _value.descriptor
          // ignore: cast_nullable_to_non_nullable
          : descriptor as SourceDescriptor,
      settings: settings == const $CopyWithPlaceholder() || settings == null
          ? _value.settings
          // ignore: cast_nullable_to_non_nullable
          : settings as SourceSettings,
      health: health == const $CopyWithPlaceholder() || health == null
          ? _value.health
          // ignore: cast_nullable_to_non_nullable
          : health as SourceHealth,
    );
  }
}

extension $SourceDetailCopyWith on SourceDetail {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSourceDetail.copyWith(...)` or `instanceOfSourceDetail.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SourceDetailCWProxy get copyWith => _$SourceDetailCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SourceDetail _$SourceDetailFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SourceDetail', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['descriptor', 'settings', 'health'],
      );
      final val = SourceDetail(
        descriptor: $checkedConvert(
          'descriptor',
          (v) => SourceDescriptor.fromJson(v as Map<String, dynamic>),
        ),
        settings: $checkedConvert(
          'settings',
          (v) => SourceSettings.fromJson(v as Map<String, dynamic>),
        ),
        health: $checkedConvert(
          'health',
          (v) => SourceHealth.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SourceDetailToJson(SourceDetail instance) =>
    <String, dynamic>{
      'descriptor': instance.descriptor.toJson(),
      'settings': instance.settings.toJson(),
      'health': instance.health.toJson(),
    };
