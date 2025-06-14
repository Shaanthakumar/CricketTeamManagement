import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlayerListComponent as PLayerlistComponent } from './playerlist';

describe('Playerlist', () => {
  let component: PLayerlistComponent;
  let fixture: ComponentFixture<PLayerlistComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PLayerlistComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PLayerlistComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
