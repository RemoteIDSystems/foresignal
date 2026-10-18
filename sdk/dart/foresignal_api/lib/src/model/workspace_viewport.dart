//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'workspace_viewport.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class WorkspaceViewport {
  /// Returns a new [WorkspaceViewport] instance.
  WorkspaceViewport({

     this.bbox,

     this.center,

     this.radiusKm,
  });

      /// [minLon, minLat, maxLon, maxLat]
  @JsonKey(
    
    name: r'bbox',
    required: false,
    includeIfNull: false,
  )


  final List<num>? bbox;



      /// [lon, lat]
  @JsonKey(
    
    name: r'center',
    required: false,
    includeIfNull: false,
  )


  final List<num>? center;



          // minimum: 0
  @JsonKey(
    
    name: r'radiusKm',
    required: false,
    includeIfNull: false,
  )


  final num? radiusKm;





    @override
    bool operator ==(Object other) => identical(this, other) || other is WorkspaceViewport &&
      other.bbox == bbox &&
      other.center == center &&
      other.radiusKm == radiusKm;

    @override
    int get hashCode =>
        bbox.hashCode +
        center.hashCode +
        radiusKm.hashCode;

  factory WorkspaceViewport.fromJson(Map<String, dynamic> json) => _$WorkspaceViewportFromJson(json);

  Map<String, dynamic> toJson() => _$WorkspaceViewportToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

