// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'intel_corroboration.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IntelCorroborationCWProxy {
  IntelCorroboration eventQuality(IntelCorroborationEventQuality? eventQuality);

  IntelCorroboration situationActivation(
    IntelCorroborationSituationActivation? situationActivation,
  );

  IntelCorroboration autoCorroborate(
    IntelCorroborationAutoCorroborate? autoCorroborate,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelCorroboration(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelCorroboration(...).copyWith(id: 12, name: "My name")
  /// ```
  IntelCorroboration call({
    IntelCorroborationEventQuality? eventQuality,
    IntelCorroborationSituationActivation? situationActivation,
    IntelCorroborationAutoCorroborate? autoCorroborate,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIntelCorroboration.copyWith(...)` or call `instanceOfIntelCorroboration.copyWith.fieldName(value)` for a single field.
class _$IntelCorroborationCWProxyImpl implements _$IntelCorroborationCWProxy {
  const _$IntelCorroborationCWProxyImpl(this._value);

  final IntelCorroboration _value;

  @override
  IntelCorroboration eventQuality(
    IntelCorroborationEventQuality? eventQuality,
  ) => call(eventQuality: eventQuality);

  @override
  IntelCorroboration situationActivation(
    IntelCorroborationSituationActivation? situationActivation,
  ) => call(situationActivation: situationActivation);

  @override
  IntelCorroboration autoCorroborate(
    IntelCorroborationAutoCorroborate? autoCorroborate,
  ) => call(autoCorroborate: autoCorroborate);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelCorroboration(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelCorroboration(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IntelCorroboration call({
    Object? eventQuality = const $CopyWithPlaceholder(),
    Object? situationActivation = const $CopyWithPlaceholder(),
    Object? autoCorroborate = const $CopyWithPlaceholder(),
  }) {
    return IntelCorroboration(
      eventQuality: eventQuality == const $CopyWithPlaceholder()
          ? _value.eventQuality
          // ignore: cast_nullable_to_non_nullable
          : eventQuality as IntelCorroborationEventQuality?,
      situationActivation: situationActivation == const $CopyWithPlaceholder()
          ? _value.situationActivation
          // ignore: cast_nullable_to_non_nullable
          : situationActivation as IntelCorroborationSituationActivation?,
      autoCorroborate: autoCorroborate == const $CopyWithPlaceholder()
          ? _value.autoCorroborate
          // ignore: cast_nullable_to_non_nullable
          : autoCorroborate as IntelCorroborationAutoCorroborate?,
    );
  }
}

extension $IntelCorroborationCopyWith on IntelCorroboration {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIntelCorroboration.copyWith(...)` or `instanceOfIntelCorroboration.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IntelCorroborationCWProxy get copyWith =>
      _$IntelCorroborationCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IntelCorroboration _$IntelCorroborationFromJson(Map<String, dynamic> json) =>
    $checkedCreate('IntelCorroboration', json, ($checkedConvert) {
      final val = IntelCorroboration(
        eventQuality: $checkedConvert(
          'eventQuality',
          (v) => v == null
              ? null
              : IntelCorroborationEventQuality.fromJson(
                  v as Map<String, dynamic>,
                ),
        ),
        situationActivation: $checkedConvert(
          'situationActivation',
          (v) => v == null
              ? null
              : IntelCorroborationSituationActivation.fromJson(
                  v as Map<String, dynamic>,
                ),
        ),
        autoCorroborate: $checkedConvert(
          'autoCorroborate',
          (v) => v == null
              ? null
              : IntelCorroborationAutoCorroborate.fromJson(
                  v as Map<String, dynamic>,
                ),
        ),
      );
      return val;
    });

Map<String, dynamic> _$IntelCorroborationToJson(IntelCorroboration instance) =>
    <String, dynamic>{
      'eventQuality': ?instance.eventQuality?.toJson(),
      'situationActivation': ?instance.situationActivation?.toJson(),
      'autoCorroborate': ?instance.autoCorroborate?.toJson(),
    };
