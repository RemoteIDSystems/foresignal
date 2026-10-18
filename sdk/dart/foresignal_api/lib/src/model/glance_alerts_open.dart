//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'glance_alerts_open.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class GlanceAlertsOpen {
  /// Returns a new [GlanceAlertsOpen] instance.
  GlanceAlertsOpen({

     this.critical,

     this.warning,

     this.info,
  });

  @JsonKey(
    
    name: r'critical',
    required: false,
    includeIfNull: false,
  )


  final int? critical;



  @JsonKey(
    
    name: r'warning',
    required: false,
    includeIfNull: false,
  )


  final int? warning;



  @JsonKey(
    
    name: r'info',
    required: false,
    includeIfNull: false,
  )


  final int? info;





    @override
    bool operator ==(Object other) => identical(this, other) || other is GlanceAlertsOpen &&
      other.critical == critical &&
      other.warning == warning &&
      other.info == info;

    @override
    int get hashCode =>
        critical.hashCode +
        warning.hashCode +
        info.hashCode;

  factory GlanceAlertsOpen.fromJson(Map<String, dynamic> json) => _$GlanceAlertsOpenFromJson(json);

  Map<String, dynamic> toJson() => _$GlanceAlertsOpenToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

